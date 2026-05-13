package com.example.pos.product;

import com.example.pos.product.dto.ProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/products";

    // ------------------------------------------------------------------ helpers

    private ProductRequest buildRequest(String name, String barcode, String sku,
                                        BigDecimal price, int stock) {
        return ProductRequest.builder()
                .name(name)
                .barcode(barcode)
                .sku(sku)
                .price(price)
                .stock(stock)
                .build();
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ------------------------------------------------------------------ POST /products

    @Test
    void createProduct_validRequest_returns201() throws Exception {
        ProductRequest req = buildRequest("Banana", "BC-001", "SKU-B1",
                new BigDecimal("0.99"), 50);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.barcode").value("BC-001"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createProduct_missingName_returns400() throws Exception {
        ProductRequest req = buildRequest(null, "BC-002", null,
                new BigDecimal("1.00"), 10);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_negativePriceReturns400() throws Exception {
        ProductRequest req = buildRequest("Item", "BC-003", null,
                new BigDecimal("-1.00"), 10);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_negativeStockReturns400() throws Exception {
        ProductRequest req = buildRequest("Item", "BC-004", null,
                new BigDecimal("1.00"), -5);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_duplicateBarcode_returns409() throws Exception {
        ProductRequest req = buildRequest("Apple", "BC-DUP", null,
                new BigDecimal("1.00"), 10);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void createProduct_duplicateSku_returns409() throws Exception {
        ProductRequest req1 = buildRequest("Apple", "BC-A1", "SKU-DUP",
                new BigDecimal("1.00"), 10);
        ProductRequest req2 = buildRequest("Orange", "BC-A2", "SKU-DUP",
                new BigDecimal("2.00"), 5);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req2)))
                .andExpect(status().isConflict());
    }

    // ------------------------------------------------------------------ GET /products

    @Test
    void listAll_returnsOnlyActiveProducts() throws Exception {
        ProductRequest req = buildRequest("Mango", "BC-M1", null,
                new BigDecimal("2.50"), 20);

        String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // soft-delete it
        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        // should not appear in list
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")]").doesNotExist());
    }

    // ------------------------------------------------------------------ GET /products/{id}

    @Test
    void getById_existingProduct_returns200() throws Exception {
        ProductRequest req = buildRequest("Grape", "BC-G1", null,
                new BigDecimal("3.00"), 15);

        String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Grape"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/9999"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ GET /products/barcode/{barcode}

    @Test
    void getByBarcode_existingProduct_returns200() throws Exception {
        ProductRequest req = buildRequest("Pear", "BC-P1", null,
                new BigDecimal("1.20"), 30);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/barcode/BC-P1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barcode").value("BC-P1"));
    }

    @Test
    void getByBarcode_notFound_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/barcode/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ GET /products/search

    @Test
    void searchByName_partialCaseInsensitive_returnsMatches() throws Exception {
        ProductRequest req = buildRequest("Strawberry", "BC-S1", null,
                new BigDecimal("4.00"), 10);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/search").param("name", "straw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", containsStringIgnoringCase("straw")));
    }

    // ------------------------------------------------------------------ PUT /products/{id}

    @Test
    void updateProduct_validRequest_returns200() throws Exception {
        ProductRequest req = buildRequest("Lemon", "BC-L1", null,
                new BigDecimal("0.80"), 40);

        String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        ProductRequest updated = buildRequest("Lemon Updated", "BC-L1", null,
                new BigDecimal("0.90"), 45);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lemon Updated"))
                .andExpect(jsonPath("$.price").value(0.90));
    }

    // ------------------------------------------------------------------ DELETE /products/{id}

    @Test
    void deleteProduct_existingProduct_returns204() throws Exception {
        ProductRequest req = buildRequest("Kiwi", "BC-K1", null,
                new BigDecimal("1.50"), 25);

        String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        // Verify soft-delete: GET should return 404
        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_notFound_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/9999"))
                .andExpect(status().isNotFound());
    }
}
