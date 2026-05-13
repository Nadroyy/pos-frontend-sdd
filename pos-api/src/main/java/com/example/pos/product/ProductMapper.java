package com.example.pos.product;

import com.example.pos.product.dto.ProductRequest;
import com.example.pos.product.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest req) {
        return Product.builder()
                .name(req.getName())
                .barcode(req.getBarcode())
                .sku(blankToNull(req.getSku()))
                .description(req.getDescription())
                .category(req.getCategory())
                .price(req.getPrice())
                .stock(req.getStock())
                .active(true)
                .build();
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .barcode(product.getBarcode())
                .sku(product.getSku())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .stock(product.getStock())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public void updateEntity(Product product, ProductRequest req) {
        product.setName(req.getName());
        product.setBarcode(req.getBarcode());
        product.setSku(blankToNull(req.getSku()));
        product.setDescription(req.getDescription());
        product.setCategory(req.getCategory());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
