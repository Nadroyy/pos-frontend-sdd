package com.example.pos.sale.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to add a product to a sale.
 * Provide either productId OR barcode (productId takes precedence).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequest {

    /** Optional – use productId or barcode. */
    private Long productId;

    /** Optional – use productId or barcode. */
    private String barcode;

    @Min(value = 1, message = "Quantity must be >= 1")
    private int quantity;
}
