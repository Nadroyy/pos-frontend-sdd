package com.example.pos.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Exact barcode lookup (active or not). */
    Optional<Product> findByBarcode(String barcode);

    /** Exact barcode lookup restricted to active products. */
    Optional<Product> findByBarcodeAndActiveTrue(String barcode);

    /** Case-insensitive partial name search among active products. */
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    /** All active products. */
    List<Product> findByActiveTrue();

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);
}
