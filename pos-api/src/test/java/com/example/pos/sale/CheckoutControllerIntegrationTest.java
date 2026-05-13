package com.example.pos.sale;

import com.example.pos.product.dto.ProductRequest;
import com.example.pos.sale.dto.AddItemRequest;
import com.example.pos.sale.dto.CheckoutRequest;
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
class CheckoutControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String SALES_URL    = "/api/v1/sales";
    private static final String PRODUCTS_URL = "/api/v1/products";

    private Long productId;

    @BeforeEach
    void setup() throws Exception {
        ProductRequest req = ProductRequest.builder()
                .name("Widget").barcode("BC-W1").sku(null)
                .price(new BigDecimal("10.00")).stock(50).build();

        String resp = mockMvc.perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        productId = objectMapper.readTree(resp).get("id").asLong();
    }

    // ---------------------------------------------------------------- helpers

    private Long createSaleWithItem(int qty) throws Exception {
        // create sale
        String saleResp = mockMvc.perform(post(SALES_URL))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long saleId = objectMapper.readTree(saleResp).get("id").asLong();

        // add item
        AddItemRequest addReq = AddItemRequest.builder()
                .productId(productId).quantity(qty).build();
        mockMvc.perform(post(SALES_URL + "/" + saleId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk());

        return saleId;
    }

    private Long createEmptySale() throws Exception {
        String resp = mockMvc.perform(post(SALES_URL))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asLong();
    }

    // ---------------------------------------------------------------- CASH

    @Test
    void checkout_cash_exactAmount_returns200WithCompleted() throws Exception {
        Long saleId = createSaleWithItem(1); // total = 10 + 1.90 = 11.90

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("11.90"))
                .build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.paymentType").value("CASH"))
                .andExpect(jsonPath("$.changeAmount").value(0.00))
                .andExpect(jsonPath("$.transactionId").isNotEmpty())
                .andExpect(jsonPath("$.completedAt").isNotEmpty());
    }

    @Test
    void checkout_cash_withChange_calculatesChange() throws Exception {
        Long saleId = createSaleWithItem(1); // total = 11.90

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("20.00"))
                .build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changeAmount").value(8.10));
    }

    @Test
    void checkout_cash_insufficientAmount_returns400() throws Exception {
        Long saleId = createSaleWithItem(1);

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("5.00"))
                .build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- CARD

    @Test
    void checkout_card_validReference_returns200() throws Exception {
        Long saleId = createSaleWithItem(2);

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CARD)
                .paymentReference("AUTH-99999")
                .build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.paymentType").value("CARD"))
                .andExpect(jsonPath("$.paymentReference").value("AUTH-99999"));
    }

    @Test
    void checkout_card_missingReference_returns400() throws Exception {
        Long saleId = createSaleWithItem(1);

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CARD)
                .build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- CREDIT

    @Test
    void checkout_credit_returns200WithCreditReference() throws Exception {
        Long saleId = createSaleWithItem(1);

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CREDIT)
                .build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.paymentType").value("CREDIT"))
                .andExpect(jsonPath("$.creditReferenceNumber", startsWith("CRED-")));
    }

    // ---------------------------------------------------------------- VALIDATIONS

    @Test
    void checkout_emptySale_returns400() throws Exception {
        Long saleId = createEmptySale();

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("20.00"))
                .build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_missingPaymentType_returns400() throws Exception {
        Long saleId = createSaleWithItem(1);

        // Send empty body – paymentType is @NotNull
        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_alreadyCompleted_returns400() throws Exception {
        Long saleId = createSaleWithItem(1);

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CARD)
                .paymentReference("AUTH-111")
                .build();

        // First checkout
        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Second checkout on same sale
        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- MODIFY AFTER CHECKOUT

    @Test
    void addItem_afterCheckout_returns400() throws Exception {
        Long saleId = createSaleWithItem(1);

        CheckoutRequest checkoutReq = CheckoutRequest.builder()
                .paymentType(PaymentType.CARD)
                .paymentReference("AUTH-222")
                .build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutReq)))
                .andExpect(status().isOk());

        AddItemRequest addReq = AddItemRequest.builder()
                .productId(productId).quantity(1).build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- RECEIPT

    @Test
    void getReceipt_completedSale_returns200WithAllFields() throws Exception {
        Long saleId = createSaleWithItem(2);

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("30.00"))
                .build();

        mockMvc.perform(post(SALES_URL + "/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(get(SALES_URL + "/" + saleId + "/receipt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").value(saleId))
                .andExpect(jsonPath("$.transactionId").isNotEmpty())
                .andExpect(jsonPath("$.completedAt").isNotEmpty())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName").value("Widget"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.subtotal").value(20.00))
                .andExpect(jsonPath("$.taxAmount").value(3.80))
                .andExpect(jsonPath("$.total").value(23.80))
                .andExpect(jsonPath("$.paymentType").value("CASH"))
                .andExpect(jsonPath("$.changeAmount").isNumber());
    }

    @Test
    void getReceipt_activeSale_returns400() throws Exception {
        Long saleId = createSaleWithItem(1);

        mockMvc.perform(get(SALES_URL + "/" + saleId + "/receipt"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReceipt_notFound_returns404() throws Exception {
        mockMvc.perform(get(SALES_URL + "/9999/receipt"))
                .andExpect(status().isNotFound());
    }
}
