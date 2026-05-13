package com.example.pos.sale;

import com.example.pos.common.exception.BusinessRuleException;
import com.example.pos.common.exception.ResourceNotFoundException;
import com.example.pos.product.Product;
import com.example.pos.product.ProductService;
import com.example.pos.sale.dto.AddItemRequest;
import com.example.pos.sale.dto.SaleResponse;
import com.example.pos.sale.dto.UpdateItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private SaleMapper saleMapper;
    @Mock private ProductService productService;

    @InjectMocks private SaleService service;

    private Product product;
    private Sale activeSale;
    private SaleResponse dummyResponse;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L).name("Apple").barcode("BC-001")
                .price(new BigDecimal("1.50")).stock(100).active(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        activeSale = Sale.builder()
                .id(10L).status(SaleStatus.ACTIVE)
                .items(new ArrayList<>())
                .subtotal(BigDecimal.ZERO).taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO).total(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        dummyResponse = SaleResponse.builder().id(10L).status(SaleStatus.ACTIVE).build();
    }

    // ---------------------------------------------------------------- createSale

    @Test
    void createSale_returnsSaleWithActiveStatus() {
        when(saleRepository.save(any(Sale.class))).thenReturn(activeSale);
        when(saleMapper.toResponse(activeSale)).thenReturn(dummyResponse);

        SaleResponse result = service.createSale();

        assertThat(result.getStatus()).isEqualTo(SaleStatus.ACTIVE);
        verify(saleRepository).save(any(Sale.class));
    }

    // ---------------------------------------------------------------- findById

    @Test
    void findById_existingSale_returnsResponse() {
        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSale));
        when(saleMapper.toResponse(activeSale)).thenReturn(dummyResponse);

        SaleResponse result = service.findById(10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void findById_notFound_throwsResourceNotFoundException() {
        when(saleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------------------------------------------------------- addItem

    @Test
    void addItem_byProductId_addsItemAndRecalculates() {
        AddItemRequest req = AddItemRequest.builder().productId(1L).quantity(2).build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSale));
        when(productService.getEntityById(1L)).thenReturn(product);
        when(saleRepository.save(activeSale)).thenReturn(activeSale);
        when(saleMapper.toResponse(activeSale)).thenReturn(dummyResponse);

        service.addItem(10L, req);

        assertThat(activeSale.getItems()).hasSize(1);
        assertThat(activeSale.getItems().get(0).getQuantity()).isEqualTo(2);
        verify(saleRepository).save(activeSale);
    }

    @Test
    void addItem_byBarcode_addsItem() {
        AddItemRequest req = AddItemRequest.builder().barcode("BC-001").quantity(1).build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSale));
        when(productService.getEntityByBarcode("BC-001")).thenReturn(product);
        when(saleRepository.save(activeSale)).thenReturn(activeSale);
        when(saleMapper.toResponse(activeSale)).thenReturn(dummyResponse);

        service.addItem(10L, req);

        assertThat(activeSale.getItems()).hasSize(1);
    }

    @Test
    void addItem_sameProductTwice_incrementsQuantity() {
        AddItemRequest req = AddItemRequest.builder().productId(1L).quantity(3).build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSale));
        when(productService.getEntityById(1L)).thenReturn(product);
        when(saleRepository.save(activeSale)).thenReturn(activeSale);
        when(saleMapper.toResponse(activeSale)).thenReturn(dummyResponse);

        service.addItem(10L, req);
        service.addItem(10L, req);

        assertThat(activeSale.getItems()).hasSize(1);
        assertThat(activeSale.getItems().get(0).getQuantity()).isEqualTo(6);
    }

    @Test
    void addItem_insufficientStock_throwsBusinessRuleException() {
        product.setStock(1);
        AddItemRequest req = AddItemRequest.builder().productId(1L).quantity(5).build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSale));
        when(productService.getEntityById(1L)).thenReturn(product);

        assertThatThrownBy(() -> service.addItem(10L, req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void addItem_noProductIdOrBarcode_throwsBusinessRuleException() {
        AddItemRequest req = AddItemRequest.builder().quantity(1).build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSale));

        assertThatThrownBy(() -> service.addItem(10L, req))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void addItem_nonActiveSale_throwsBusinessRuleException() {
        activeSale.setStatus(SaleStatus.FROZEN);
        AddItemRequest req = AddItemRequest.builder().productId(1L).quantity(1).build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSale));

        assertThatThrownBy(() -> service.addItem(10L, req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("FROZEN");
    }

    // ---------------------------------------------------------------- updateItem

    @Test
    void updateItem_validQty_updatesItem() {
        // Pre-populate one item
        SaleItem item = SaleItem.builder()
                .id(1L).sale(activeSale).productId(1L)
                .productName("Apple").barcode("BC-001")
                .unitPrice(new BigDecimal("1.50")).quantity(2)
                .subtotal(new BigDecimal("3.00")).build();
        activeSale.getItems().add(item);

        UpdateItemRequest req = UpdateItemRequest.builder().quantity(5).build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSale));
        when(productService.getEntityById(1L)).thenReturn(product);
        when(saleRepository.save(activeSale)).thenReturn(activeSale);
        when(saleMapper.toResponse(activeSale)).thenReturn(dummyResponse);

        service.updateItem(10L, 1L, req);

        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    void updateItem_itemNotInSale_throwsResourceNotFoundException() {
        UpdateItemRequest req = UpdateItemRequest.builder().quantity(1).build();

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSale));

        assertThatThrownBy(() -> service.updateItem(10L, 999L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------------------------------------------------------- removeItem

    @Test
    void removeItem_existingItem_removesIt() {
        SaleItem item = SaleItem.builder()
                .id(1L).sale(activeSale).productId(1L)
                .productName("Apple").barcode("BC-001")
                .unitPrice(new BigDecimal("1.50")).quantity(2)
                .subtotal(new BigDecimal("3.00")).build();
        activeSale.getItems().add(item);

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSale));
        when(saleRepository.save(activeSale)).thenReturn(activeSale);
        when(saleMapper.toResponse(activeSale)).thenReturn(dummyResponse);

        service.removeItem(10L, 1L);

        assertThat(activeSale.getItems()).isEmpty();
    }

    @Test
    void removeItem_nonActiveSale_throwsBusinessRuleException() {
        activeSale.setStatus(SaleStatus.CANCELLED);

        when(saleRepository.findById(10L)).thenReturn(Optional.of(activeSale));

        assertThatThrownBy(() -> service.removeItem(10L, 1L))
                .isInstanceOf(BusinessRuleException.class);
    }
}
