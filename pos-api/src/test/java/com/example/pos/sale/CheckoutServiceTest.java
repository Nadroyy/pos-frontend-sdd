package com.example.pos.sale;

import com.example.pos.common.exception.BusinessRuleException;
import com.example.pos.common.exception.ResourceNotFoundException;
import com.example.pos.product.Product;
import com.example.pos.product.ProductService;
import com.example.pos.sale.dto.CheckoutRequest;
import com.example.pos.sale.dto.ReceiptResponse;
import com.example.pos.sale.dto.SaleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private SaleMapper saleMapper;
    @Mock private ProductService productService;

    @InjectMocks private CheckoutService checkoutService;

    private Product product;
    private Sale activeSaleWithItem;
    private SaleItem saleItem;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L).name("Apple").barcode("BC-001")
                .price(new BigDecimal("10.00")).stock(50).active(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        activeSaleWithItem = Sale.builder()
                .id(10L).status(SaleStatus.ACTIVE)
                .items(new ArrayList<>())
                .subtotal(new BigDecimal("10.00"))
                .taxAmount(new BigDecimal("1.90"))
                .discountAmount(BigDecimal.ZERO)
                .total(new BigDecimal("11.90"))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        saleItem = SaleItem.builder()
                .id(1L).sale(activeSaleWithItem).productId(1L)
                .productName("Apple").barcode("BC-001")
                .unitPrice(new BigDecimal("10.00")).quantity(1)
                .subtotal(new BigDecimal("10.00")).build();

        activeSaleWithItem.getItems().add(saleItem);
    }

    // ---------------------------------------------------------------- CASH

    @Test
    void checkout_cash_exactAmount_completesSuccessfully() {
        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("11.90"))
                .build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));
        when(productService.getEntityById(1L)).thenReturn(product);
        when(saleRepository.save(any())).thenReturn(activeSaleWithItem);
        when(saleMapper.toResponse(any())).thenReturn(SaleResponse.builder().id(10L).status(SaleStatus.COMPLETED).build());

        SaleResponse result = checkoutService.checkout(10L, req);

        assertThat(activeSaleWithItem.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(activeSaleWithItem.getPaymentType()).isEqualTo(PaymentType.CASH);
        assertThat(activeSaleWithItem.getChangeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(activeSaleWithItem.getTransactionId()).isNotBlank();
        assertThat(activeSaleWithItem.getCompletedAt()).isNotNull();
    }

    @Test
    void checkout_cash_withChange_calculatesChangeCorrectly() {
        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("20.00"))
                .build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));
        when(productService.getEntityById(1L)).thenReturn(product);
        when(saleRepository.save(any())).thenReturn(activeSaleWithItem);
        when(saleMapper.toResponse(any())).thenReturn(SaleResponse.builder().build());

        checkoutService.checkout(10L, req);

        assertThat(activeSaleWithItem.getChangeAmount())
                .isEqualByComparingTo(new BigDecimal("8.10")); // 20.00 - 11.90
    }

    @Test
    void checkout_cash_insufficientAmount_throwsBusinessRuleException() {
        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("5.00"))
                .build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));
        when(productService.getEntityById(1L)).thenReturn(product);

        assertThatThrownBy(() -> checkoutService.checkout(10L, req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient cash");
    }

    @Test
    void checkout_cash_missingAmountReceived_throwsBusinessRuleException() {
        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));
        when(productService.getEntityById(1L)).thenReturn(product);

        assertThatThrownBy(() -> checkoutService.checkout(10L, req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("amountReceived is required");
    }

    // ---------------------------------------------------------------- CARD

    @Test
    void checkout_card_validReference_completesSuccessfully() {
        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CARD)
                .paymentReference("AUTH-12345")
                .build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));
        when(productService.getEntityById(1L)).thenReturn(product);
        when(saleRepository.save(any())).thenReturn(activeSaleWithItem);
        when(saleMapper.toResponse(any())).thenReturn(SaleResponse.builder().build());

        checkoutService.checkout(10L, req);

        assertThat(activeSaleWithItem.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(activeSaleWithItem.getPaymentType()).isEqualTo(PaymentType.CARD);
        assertThat(activeSaleWithItem.getPaymentReference()).isEqualTo("AUTH-12345");
        assertThat(activeSaleWithItem.getChangeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void checkout_card_missingReference_throwsBusinessRuleException() {
        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CARD)
                .build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));
        when(productService.getEntityById(1L)).thenReturn(product);

        assertThatThrownBy(() -> checkoutService.checkout(10L, req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("paymentReference is required");
    }

    // ---------------------------------------------------------------- CREDIT

    @Test
    void checkout_credit_generatesCreditReferenceNumber() {
        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CREDIT)
                .build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));
        when(productService.getEntityById(1L)).thenReturn(product);
        when(saleRepository.save(any())).thenReturn(activeSaleWithItem);
        when(saleMapper.toResponse(any())).thenReturn(SaleResponse.builder().build());

        checkoutService.checkout(10L, req);

        assertThat(activeSaleWithItem.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(activeSaleWithItem.getPaymentType()).isEqualTo(PaymentType.CREDIT);
        assertThat(activeSaleWithItem.getCreditReferenceNumber()).startsWith("CRED-");
    }

    // ---------------------------------------------------------------- VALIDATIONS

    @Test
    void checkout_emptySale_throwsBusinessRuleException() {
        activeSaleWithItem.getItems().clear();

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("20.00"))
                .build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));

        assertThatThrownBy(() -> checkoutService.checkout(10L, req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("empty sale");
    }

    @Test
    void checkout_nonActiveSale_throwsBusinessRuleException() {
        activeSaleWithItem.setStatus(SaleStatus.COMPLETED);

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("20.00"))
                .build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));

        assertThatThrownBy(() -> checkoutService.checkout(10L, req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    void checkout_insufficientStock_throwsBusinessRuleException() {
        product.setStock(0);

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("20.00"))
                .build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));
        when(productService.getEntityById(1L)).thenReturn(product);

        assertThatThrownBy(() -> checkoutService.checkout(10L, req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void checkout_saleNotFound_throwsResourceNotFoundException() {
        when(saleRepository.findById(99L)).thenReturn(Optional.empty());

        CheckoutRequest req = CheckoutRequest.builder()
                .paymentType(PaymentType.CASH)
                .amountReceived(new BigDecimal("20.00"))
                .build();

        assertThatThrownBy(() -> checkoutService.checkout(99L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------------------------------------------------------- RECEIPT

    @Test
    void getReceipt_completedSale_returnsReceipt() {
        activeSaleWithItem.setStatus(SaleStatus.COMPLETED);
        activeSaleWithItem.setTransactionId("TX-001");
        activeSaleWithItem.setCompletedAt(LocalDateTime.now());
        activeSaleWithItem.setPaymentType(PaymentType.CASH);

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));
        when(saleMapper.toReceipt(activeSaleWithItem))
                .thenReturn(ReceiptResponse.builder().saleId(10L).transactionId("TX-001").build());

        ReceiptResponse receipt = checkoutService.getReceipt(10L);

        assertThat(receipt.getSaleId()).isEqualTo(10L);
        assertThat(receipt.getTransactionId()).isEqualTo("TX-001");
    }

    @Test
    void getReceipt_nonCompletedSale_throwsBusinessRuleException() {
        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSaleWithItem));

        assertThatThrownBy(() -> checkoutService.getReceipt(10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("COMPLETED");
    }
}
