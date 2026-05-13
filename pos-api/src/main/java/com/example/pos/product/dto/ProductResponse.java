package com.example.pos.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String barcode;
    private String sku;
    private String description;
    private String category;
    private BigDecimal price;
    private int stock;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
