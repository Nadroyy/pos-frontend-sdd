package com.example.pos.product;

import com.example.pos.common.exception.ConflictException;
import com.example.pos.common.exception.ResourceNotFoundException;
import com.example.pos.product.dto.ProductRequest;
import com.example.pos.product.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductService service;

    private Product sampleProduct;
    private ProductRequest sampleRequest;
    private ProductResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Apple")
                .barcode("1234567890")
                .sku("SKU-001")
                .price(new BigDecimal("1.50"))
                .stock(100)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = ProductRequest.builder()
                .name("Apple")
                .barcode("1234567890")
                .sku("SKU-001")
                .price(new BigDecimal("1.50"))
                .stock(100)
                .build();

        sampleResponse = ProductResponse.builder()
                .id(1L)
                .name("Apple")
                .barcode("1234567890")
                .sku("SKU-001")
                .price(new BigDecimal("1.50"))
                .stock(100)
                .active(true)
                .build();
    }

    // ------------------------------------------------------------------ findAll

    @Test
    void findAll_returnsActiveProducts() {
        when(repository.findByActiveTrue()).thenReturn(List.of(sampleProduct));
        when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        List<ProductResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBarcode()).isEqualTo("1234567890");
    }

    // ------------------------------------------------------------------ findById

    @Test
    void findById_existingActive_returnsResponse() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        ProductResponse result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_notFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_inactiveProduct_throwsResourceNotFoundException() {
        sampleProduct.setActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------ findByBarcode

    @Test
    void findByBarcode_existingActive_returnsResponse() {
        when(repository.findByBarcodeAndActiveTrue("1234567890"))
                .thenReturn(Optional.of(sampleProduct));
        when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        ProductResponse result = service.findByBarcode("1234567890");

        assertThat(result.getBarcode()).isEqualTo("1234567890");
    }

    @Test
    void findByBarcode_notFound_throwsResourceNotFoundException() {
        when(repository.findByBarcodeAndActiveTrue("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByBarcode("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------ searchByName

    @Test
    void searchByName_returnsMatchingProducts() {
        when(repository.findByNameContainingIgnoreCaseAndActiveTrue("app"))
                .thenReturn(List.of(sampleProduct));
        when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        List<ProductResponse> result = service.searchByName("app");

        assertThat(result).hasSize(1);
    }

    // ------------------------------------------------------------------ create

    @Test
    void create_validRequest_returnsCreatedProduct() {
        when(repository.existsByBarcode("1234567890")).thenReturn(false);
        when(repository.existsBySku("SKU-001")).thenReturn(false);
        when(mapper.toEntity(sampleRequest)).thenReturn(sampleProduct);
        when(repository.save(sampleProduct)).thenReturn(sampleProduct);
        when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        ProductResponse result = service.create(sampleRequest);

        assertThat(result.getBarcode()).isEqualTo("1234567890");
        verify(repository).save(sampleProduct);
    }

    @Test
    void create_duplicateBarcode_throwsConflictException() {
        when(repository.existsByBarcode("1234567890")).thenReturn(true);

        assertThatThrownBy(() -> service.create(sampleRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Barcode already in use");
    }

    @Test
    void create_duplicateSku_throwsConflictException() {
        when(repository.existsByBarcode("1234567890")).thenReturn(false);
        when(repository.existsBySku("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(sampleRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("SKU already in use");
    }

    @Test
    void create_nullSku_skipsSkuValidation() {
        sampleRequest.setSku(null);
        when(repository.existsByBarcode("1234567890")).thenReturn(false);
        when(mapper.toEntity(sampleRequest)).thenReturn(sampleProduct);
        when(repository.save(sampleProduct)).thenReturn(sampleProduct);
        when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        assertThatCode(() -> service.create(sampleRequest)).doesNotThrowAnyException();
        verify(repository, never()).existsBySku(any());
    }

    // ------------------------------------------------------------------ update

    @Test
    void update_validRequest_returnsUpdatedProduct() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(repository.existsByBarcodeAndIdNot("1234567890", 1L)).thenReturn(false);
        when(repository.existsBySkuAndIdNot("SKU-001", 1L)).thenReturn(false);
        when(repository.save(sampleProduct)).thenReturn(sampleProduct);
        when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        ProductResponse result = service.update(1L, sampleRequest);

        assertThat(result.getId()).isEqualTo(1L);
        verify(mapper).updateEntity(sampleProduct, sampleRequest);
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, sampleRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------ delete

    @Test
    void delete_existingProduct_setsActiveFalse() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(repository.save(sampleProduct)).thenReturn(sampleProduct);

        service.delete(1L);

        assertThat(sampleProduct.isActive()).isFalse();
        verify(repository).save(sampleProduct);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
