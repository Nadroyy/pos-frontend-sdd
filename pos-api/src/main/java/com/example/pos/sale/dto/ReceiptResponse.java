package com.example.pos.sale.dto;

import com.example.pos.sale.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {

    private Long saleId;
    private String transactionId;
    private LocalDateTime completedAt;

    private List<ReceiptItemLine> items;

    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal total;

    private PaymentType paymentType;
    private BigDecimal amountReceived;
    private BigDecimal changeAmount;
    private String paymentReference;
    private String creditReferenceNumber;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptItemLine {
        private String productName;
        private String barcode;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal subtotal;
    }
}
