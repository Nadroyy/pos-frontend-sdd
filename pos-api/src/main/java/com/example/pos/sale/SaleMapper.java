package com.example.pos.sale;

import com.example.pos.sale.dto.ReceiptResponse;
import com.example.pos.sale.dto.SaleItemResponse;
import com.example.pos.sale.dto.SaleResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SaleMapper {

    public SaleResponse toResponse(Sale sale) {
        List<SaleItemResponse> itemResponses = sale.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return SaleResponse.builder()
                .id(sale.getId())
                .status(sale.getStatus())
                .items(itemResponses)
                .subtotal(sale.getSubtotal())
                .taxAmount(sale.getTaxAmount())
                .discountAmount(sale.getDiscountAmount())
                .total(sale.getTotal())
                // payment fields
                .paymentType(sale.getPaymentType())
                .amountReceived(sale.getAmountReceived())
                .changeAmount(sale.getChangeAmount())
                .paymentReference(sale.getPaymentReference())
                .creditReferenceNumber(sale.getCreditReferenceNumber())
                .transactionId(sale.getTransactionId())
                .completedAt(sale.getCompletedAt())
                .createdAt(sale.getCreatedAt())
                .updatedAt(sale.getUpdatedAt())
                .build();
    }

    public SaleItemResponse toItemResponse(SaleItem item) {
        return SaleItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .barcode(item.getBarcode())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }

    public ReceiptResponse toReceipt(Sale sale) {
        List<ReceiptResponse.ReceiptItemLine> lines = sale.getItems().stream()
                .map(i -> ReceiptResponse.ReceiptItemLine.builder()
                        .productName(i.getProductName())
                        .barcode(i.getBarcode())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .build())
                .toList();

        return ReceiptResponse.builder()
                .saleId(sale.getId())
                .transactionId(sale.getTransactionId())
                .completedAt(sale.getCompletedAt())
                .items(lines)
                .subtotal(sale.getSubtotal())
                .taxAmount(sale.getTaxAmount())
                .discountAmount(sale.getDiscountAmount())
                .total(sale.getTotal())
                .paymentType(sale.getPaymentType())
                .amountReceived(sale.getAmountReceived())
                .changeAmount(sale.getChangeAmount())
                .paymentReference(sale.getPaymentReference())
                .creditReferenceNumber(sale.getCreditReferenceNumber())
                .build();
    }
}
