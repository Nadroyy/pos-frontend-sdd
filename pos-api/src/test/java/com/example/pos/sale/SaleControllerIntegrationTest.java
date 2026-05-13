package com.example.pos.sale;

import com.example.pos.product.dto.ProductRequest;
import com.example.pos.sale.dto.AddItemRequest;
import com.example.pos.sale.dto.UpdateItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
class SaleControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String SALES_URL    = "/api/v1/sales";
    private static final String PRODUCTS_URL = "/api/v1/products";

    private Long productId;
    private String productBarcode;

    @BeforeEach
    void createProduct() throws Exception {
        ProductRequest req = ProductRequest.builder()
                .name("Test Product").barcode("BC-TEST").sku(null)
                .price(new BigDecimal("10.00")).stock(50).build();

        String resp = mockMvc.perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        productId      = objectMapper.readTree(resp).get("id").asLong();
        productBarcode = objectMapper.readTree(resp).get("barcode").asText();
    }

    // ---------------------------------------------------------------- helpers

    private Long createSale() throws Exception {
        String resp = mockMvc.perform(post(SALES_URL))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asLong();
    }

    private Long addItem(Long saleId, Long pId, String barcode, int qty) throws Exception {
        AddItemRequest req = AddItemRequest.builder()
                .productId(pId).barcode(barcode).quantity(qty).build();

        String resp = mockMvc.perform(post(SALES_URL + "/" + saleId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(resp).get("items").get(0).get("id").asLong();
    }

    // ---------------------------------------------------------------- POST /sales

    @Test
    void createSale_returns201WithActiveStatus() throws Exception {
        mockMvc.perform(post(SALES_URL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    // ---------------------------------------------------------------- GET /sales/{id}

    @Test
    void getSale_existingSale_returns200() throws Exception {
        Long saleId = createSale();

        mockMvc.perform(get(SALES_URL + "/" + saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saleId));
    }

    @Test
    void getSale_notFound_returns404() throws Exception {
        mockMvc.perform(get(SALES_URL + "/9999"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- POST /sales/{id}/items

    @Test
    void addItem_byProductId_addsItemAndCalculatesTotals() throws Exception {
        Long saleId = createSale();

        AddItemRequest req = AddItemRequest.builder()
                .productId(productId).quantity(2).build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(10.00))
                .andExpect(jsonPath("$.items[0].subtotal").value(20.00))
                .andExpect(jsonPath("$.subtotal").value(20.00))
                .andExpect(jsonPath("$.taxAmount").value(3.80))
                .andExpect(jsonPath("$.total").value(23.80));
    }

    @Test
    void addItem_byBarcode_addsItem() throws Exception {
        Long saleId = createSale();

        AddItemRequest req = AddItemRequest.builder()
                .barcode(productBarcode).quantity(1).build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].barcode").value(productBarcode));
    }

    @Test
    void addItem_sameProductTwice_mergesQuantity() throws Exception {
        Long saleId = createSale();

        AddItemRequest req = AddItemRequest.builder()
                .productId(productId).quantity(3).build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(6));
    }

    @Test
    void addItem_insufficientStock_returns400() throws Exception {
        Long saleId = createSale();

        AddItemRequest req = AddItemRequest.builder()
                .productId(productId).quantity(999).build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_quantityZero_returns400() throws Exception {
        Long saleId = createSale();

        AddItemRequest req = AddItemRequest.builder()
                .productId(productId).quantity(0).build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_saleNotFound_returns404() throws Exception {
        AddItemRequest req = AddItemRequest.builder()
                .productId(productId).quantity(1).build();

        mockMvc.perform(post(SALES_URL + "/9999/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- PUT /sales/{id}/items/{itemId}

    @Test
    void updateItem_validQty_updatesQuantityAndTotals() throws Exception {
        Long saleId = createSale();
        Long itemId = addItem(saleId, productId, null, 2);

        UpdateItemRequest req = UpdateItemRequest.builder().quantity(5).build();

        mockMvc.perform(put(SALES_URL + "/" + saleId + "/items/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.subtotal").value(50.00))
                .andExpect(jsonPath("$.taxAmount").value(9.50))
                .andExpect(jsonPath("$.total").value(59.50));
    }

    @Test
    void updateItem_quantityZero_returns400() throws Exception {
        Long saleId = createSale();
        Long itemId = addItem(saleId, productId, null, 1);

        UpdateItemRequest req = UpdateItemRequest.builder().quantity(0).build();

        mockMvc.perform(put(SALES_URL + "/" + saleId + "/items/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- DELETE /sales/{id}/items/{itemId}

    @Test
    void removeItem_existingItem_removesAndRecalculates() throws Exception {
        Long saleId = createSale();
        Long itemId = addItem(saleId, productId, null, 3);

        mockMvc.perform(delete(SALES_URL + "/" + saleId + "/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void removeItem_itemNotFound_returns404() throws Exception {
        Long saleId = createSale();

        mockMvc.perform(delete(SALES_URL + "/" + saleId + "/items/9999"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- GET /sales

    @Test
    void listAll_returnsSales() throws Exception {
        createSale();
        createSale();

        mockMvc.perform(get(SALES_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }
}
