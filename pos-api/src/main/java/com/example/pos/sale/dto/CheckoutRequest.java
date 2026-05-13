package com.example.pos.sale.dto;

import com.example.pos.sale.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    /**
     * CASH: amount handed by the customer.
     * Must be >= total. Required when paymentType = CASH.
     */
    private BigDecimal amountReceived;

    /**
     * CARD: terminal reference / authorisation code.
     * Required when paymentType = CARD.
     */
    private String paymentReference;

    /**
     * CREDIT: optional customer identifier.
     * Reserved for future Customer API integration.
     */
    private Long customerId;
}
