package com.example.pos.sale;

import com.example.pos.sale.dto.AddItemRequest;
import com.example.pos.sale.dto.CheckoutRequest;
import com.example.pos.sale.dto.ReceiptResponse;
import com.example.pos.sale.dto.SaleResponse;
import com.example.pos.sale.dto.UpdateItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Shopping cart and sale management")
public class SaleController {

    private final SaleService service;
    private final CheckoutService checkoutService;

    @GetMapping
    @Operation(summary = "List all sales")
    public ResponseEntity<List<SaleResponse>> listAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sale by ID")
    public ResponseEntity<SaleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new empty sale (cart)")
    public ResponseEntity<SaleResponse> create() {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSale());
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Add a product to the sale")
    public ResponseEntity<SaleResponse> addItem(
            @PathVariable Long id,
            @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(service.addItem(id, request));
    }

    @PutMapping("/{id}/items/{itemId}")
    @Operation(summary = "Update item quantity")
    public ResponseEntity<SaleResponse> updateItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(service.updateItem(id, itemId, request));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "Remove an item from the sale")
    public ResponseEntity<SaleResponse> removeItem(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(service.removeItem(id, itemId));
    }

    @PostMapping("/{id}/checkout")
    @Operation(summary = "Complete the sale with payment")
    public ResponseEntity<SaleResponse> checkout(
            @PathVariable Long id,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(checkoutService.checkout(id, request));
    }

    @GetMapping("/{id}/receipt")
    @Operation(summary = "Get receipt for a completed sale")
    public ResponseEntity<ReceiptResponse> getReceipt(@PathVariable Long id) {
        return ResponseEntity.ok(checkoutService.getReceipt(id));
    }
}
