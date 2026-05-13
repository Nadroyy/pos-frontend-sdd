package com.example.pos.product;

import com.example.pos.common.exception.ConflictException;
import com.example.pos.common.exception.ResourceNotFoundException;
import com.example.pos.product.dto.ProductRequest;
import com.example.pos.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    // ------------------------------------------------------------------ READ

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return repository.findByActiveTrue()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return mapper.toResponse(getActiveOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ProductResponse findByBarcode(String barcode) {
        Product product = repository.findByBarcodeAndActiveTrue(barcode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with barcode: " + barcode));
        return mapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String name) {
        return repository.findByNameContainingIgnoreCaseAndActiveTrue(name)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------- WRITE

    @Transactional
    public ProductResponse create(ProductRequest request) {
        validateUniqueBarcode(request.getBarcode(), null);
        validateUniqueSku(request.getSku(), null);

        Product product = mapper.toEntity(request);
        Product saved = repository.save(product);
        log.info("Product created: id={}, barcode={}", saved.getId(), saved.getBarcode());
        return mapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getActiveOrThrow(id);

        validateUniqueBarcode(request.getBarcode(), id);
        validateUniqueSku(request.getSku(), id);

        mapper.updateEntity(product, request);
        Product saved = repository.save(product);
        log.info("Product updated: id={}", saved.getId());
        return mapper.toResponse(saved);
    }

    /** Soft delete – sets active = false. */
    @Transactional
    public void delete(Long id) {
        Product product = getActiveOrThrow(id);
        product.setActive(false);
        repository.save(product);
        log.info("Product soft-deleted: id={}", id);
    }

    // ------------------------------------------------- INTERNAL (package use)

    /** Returns the raw entity for internal use by other services. */
    @Transactional(readOnly = true)
    public Product getEntityById(Long id) {
        return getActiveOrThrow(id);
    }

    /** Returns the raw entity by barcode for internal use. */
    @Transactional(readOnly = true)
    public Product getEntityByBarcode(String barcode) {
        return repository.findByBarcodeAndActiveTrue(barcode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with barcode: " + barcode));
    }

    // ------------------------------------------------------------ HELPERS

    private Product getActiveOrThrow(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        return product;
    }

    private void validateUniqueBarcode(String barcode, Long excludeId) {
        boolean conflict = (excludeId == null)
                ? repository.existsByBarcode(barcode)
                : repository.existsByBarcodeAndIdNot(barcode, excludeId);
        if (conflict) {
            throw new ConflictException("Barcode already in use: " + barcode);
        }
    }

    private void validateUniqueSku(String sku, Long excludeId) {
        if (sku == null || sku.isBlank()) return;
        boolean conflict = (excludeId == null)
                ? repository.existsBySku(sku)
                : repository.existsBySkuAndIdNot(sku, excludeId);
        if (conflict) {
            throw new ConflictException("SKU already in use: " + sku);
        }
    }
}
