package com.example.pos.sale;

import com.example.pos.common.exception.BusinessRuleException;
import com.example.pos.common.exception.ResourceNotFoundException;
import com.example.pos.product.Product;
import com.example.pos.product.ProductService;
import com.example.pos.sale.dto.CheckoutRequest;
import com.example.pos.sale.dto.ReceiptResponse;
import com.example.pos.sale.dto.SaleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;
    private final ProductService productService;

    // ---------------------------------------------------------------- CHECKOUT

    @Transactional
    public SaleResponse checkout(Long saleId, CheckoutRequest request) {
        Sale sale = getSaleOrThrow(saleId);

        // 1. Sale must be ACTIVE
        if (sale.getStatus() != SaleStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Cannot checkout sale " + saleId + " with status " + sale.getStatus());
        }

        // 2. Sale must have at least one item
        if (sale.getItems().isEmpty()) {
            throw new BusinessRuleException("Cannot checkout an empty sale");
        }

        // 3. Re-validate stock for every item
        for (SaleItem item : sale.getItems()) {
            Product product = productService.getEntityById(item.getProductId());
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessRuleException(
                        "Insufficient stock for product '" + product.getName()
                        + "'. Available: " + product.getStock()
                        + ", in cart: " + item.getQuantity());
            }
        }

        // 4. Payment-type specific validation
        switch (request.getPaymentType()) {
            case CASH -> processCash(sale, request);
            case CARD -> processCard(sale, request);
            case CREDIT -> processCredit(sale, request);
        }

        // 5. Complete the sale
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setTransactionId(UUID.randomUUID().toString());
        sale.setCompletedAt(LocalDateTime.now());

        Sale saved = saleRepository.save(sale);
        log.info("Sale {} completed. txId={}, payment={}", saleId,
                saved.getTransactionId(), saved.getPaymentType());
        return saleMapper.toResponse(saved);
    }

    // ---------------------------------------------------------------- RECEIPT

    @Transactional(readOnly = true)
    public ReceiptResponse getReceipt(Long saleId) {
        Sale sale = getSaleOrThrow(saleId);
        if (sale.getStatus() != SaleStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Receipt is only available for COMPLETED sales. Current status: " + sale.getStatus());
        }
        return saleMapper.toReceipt(sale);
    }

    // ------------------------------------------------------------ PAYMENT HELPERS

    private void processCash(Sale sale, CheckoutRequest request) {
        if (request.getAmountReceived() == null) {
            throw new BusinessRuleException("amountReceived is required for CASH payment");
        }
        if (request.getAmountReceived().compareTo(sale.getTotal()) < 0) {
            throw new BusinessRuleException(
                    "Insufficient cash. Total: " + sale.getTotal()
                    + ", received: " + request.getAmountReceived());
        }
        sale.setPaymentType(PaymentType.CASH);
        sale.setAmountReceived(request.getAmountReceived());
        sale.setChangeAmount(request.getAmountReceived().subtract(sale.getTotal()));
    }

    private void processCard(Sale sale, CheckoutRequest request) {
        if (request.getPaymentReference() == null || request.getPaymentReference().isBlank()) {
            throw new BusinessRuleException("paymentReference is required for CARD payment");
        }
        sale.setPaymentType(PaymentType.CARD);
        sale.setPaymentReference(request.getPaymentReference());
        sale.setAmountReceived(sale.getTotal());
        sale.setChangeAmount(BigDecimal.ZERO);
    }

    private void processCredit(Sale sale, CheckoutRequest request) {
        // customerId is optional – reserved for future Customer API
        String creditRef = "CRED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        sale.setPaymentType(PaymentType.CREDIT);
        sale.setCreditReferenceNumber(creditRef);
        sale.setAmountReceived(sale.getTotal());
        sale.setChangeAmount(BigDecimal.ZERO);
    }

    // ------------------------------------------------------------ HELPERS

    private Sale getSaleOrThrow(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
    }
}
