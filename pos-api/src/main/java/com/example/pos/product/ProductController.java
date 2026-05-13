package com.example.pos.product;

import com.example.pos.product.dto.ProductRequest;
import com.example.pos.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalogue management")
public class ProductController {

    private final ProductService service;

    @GetMapping
    @Operation(summary = "List all active products")
    public ResponseEntity<List<ProductResponse>> listAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get active product by exact barcode")
    public ResponseEntity<ProductResponse> getByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(service.findByBarcode(barcode));
    }

    @GetMapping("/search")
    @Operation(summary = "Search active products by partial name (case-insensitive)")
    public ResponseEntity<List<ProductResponse>> searchByName(
            @RequestParam String name) {
        return ResponseEntity.ok(service.searchByName(name));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a product (sets active = false)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
