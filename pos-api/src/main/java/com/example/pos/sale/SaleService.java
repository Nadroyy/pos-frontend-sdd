package com.example.pos.sale;

import com.example.pos.common.exception.BusinessRuleException;
import com.example.pos.common.exception.ResourceNotFoundException;
import com.example.pos.product.Product;
import com.example.pos.product.ProductService;
import com.example.pos.sale.dto.AddItemRequest;
import com.example.pos.sale.dto.SaleResponse;
import com.example.pos.sale.dto.UpdateItemRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;
    private final ProductService productService;

    // ------------------------------------------------------------------ READ

    @Transactional(readOnly = true)
    public List<SaleResponse> findAll() {
        return saleRepository.findAll().stream()
                .map(saleMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SaleResponse findById(Long id) {
        return saleMapper.toResponse(getSaleOrThrow(id));
    }

    // ----------------------------------------------------------------- WRITE

    /** Creates a new empty sale with status ACTIVE. */
    @Transactional
    public SaleResponse createSale() {
        Sale sale = Sale.builder().build();
        Sale saved = saleRepository.save(sale);
        log.info("Sale created: id={}", saved.getId());
        return saleMapper.toResponse(saved);
    }

    /**
     * Adds a product to the sale.
     * If the product is already in the cart, increments the quantity.
     * Resolves product by productId (preferred) or barcode.
     */
    @Transactional
    public SaleResponse addItem(Long saleId, AddItemRequest request) {
        Sale sale = getSaleOrThrow(saleId);
        requireActive(sale);

        if (request.getProductId() == null && request.getBarcode() == null) {
            throw new BusinessRuleException("Either productId or barcode must be provided");
        }

        Product product = resolveProduct(request);
        int requestedQty = request.getQuantity();

        // Check if item already exists in the cart
        Optional<SaleItem> existing = sale.getItems().stream()
                .filter(i -> i.getProductId().equals(product.getId()))
                .findFirst();

        if (existing.isPresent()) {
            SaleItem item = existing.get();
            int newQty = item.getQuantity() + requestedQty;
            validateStock(product, newQty);
            item.setQuantity(newQty);
            item.recalculate();
        } else {
            validateStock(product, requestedQty);
            SaleItem item = SaleItem.builder()
                    .sale(sale)
                    .productId(product.getId())
                    .productName(product.getName())
                    .barcode(product.getBarcode())
                    .unitPrice(product.getPrice())
                    .quantity(requestedQty)
                    .subtotal(product.getPrice())   // will be recalculated below
                    .build();
            item.recalculate();
            sale.getItems().add(item);
        }

        sale.recalculateTotals();
        Sale saved = saleRepository.save(sale);
        log.info("Item added to sale {}: productId={}, qty={}", saleId, product.getId(), requestedQty);
        return saleMapper.toResponse(saved);
    }

    /** Updates the quantity of an existing item. */
    @Transactional
    public SaleResponse updateItem(Long saleId, Long itemId, UpdateItemRequest request) {
        Sale sale = getSaleOrThrow(saleId);
        requireActive(sale);

        SaleItem item = getItemOrThrow(sale, itemId);

        // Re-fetch product to validate current stock
        Product product = productService.getEntityById(item.getProductId());
        validateStock(product, request.getQuantity());

        item.setQuantity(request.getQuantity());
        item.recalculate();
        sale.recalculateTotals();

        Sale saved = saleRepository.save(sale);
        log.info("Item {} updated in sale {}: qty={}", itemId, saleId, request.getQuantity());
        return saleMapper.toResponse(saved);
    }

    /** Removes an item from the sale. */
    @Transactional
    public SaleResponse removeItem(Long saleId, Long itemId) {
        Sale sale = getSaleOrThrow(saleId);
        requireActive(sale);

        SaleItem item = getItemOrThrow(sale, itemId);
        sale.getItems().remove(item);
        sale.recalculateTotals();

        Sale saved = saleRepository.save(sale);
        log.info("Item {} removed from sale {}", itemId, saleId);
        return saleMapper.toResponse(saved);
    }

    // ------------------------------------------------------------ HELPERS

    private Sale getSaleOrThrow(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
    }

    private SaleItem getItemOrThrow(Sale sale, Long itemId) {
        return sale.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item not found with id: " + itemId + " in sale: " + sale.getId()));
    }

    private void requireActive(Sale sale) {
        if (sale.getStatus() != SaleStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Cannot modify sale " + sale.getId() + " with status " + sale.getStatus());
        }
    }

    private Product resolveProduct(AddItemRequest request) {
        if (request.getProductId() != null) {
            return productService.getEntityById(request.getProductId());
        }
        return productService.getEntityByBarcode(request.getBarcode());
    }

    private void validateStock(Product product, int requestedQty) {
        if (product.getStock() < requestedQty) {
            throw new BusinessRuleException(
                    "Insufficient stock for product '" + product.getName()
                    + "'. Available: " + product.getStock()
                    + ", requested: " + requestedQty);
        }
    }
}
