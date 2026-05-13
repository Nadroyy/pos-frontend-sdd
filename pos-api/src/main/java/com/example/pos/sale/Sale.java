package com.example.pos.sale;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Sale {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.19");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SaleStatus status = SaleStatus.ACTIVE;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<SaleItem> items = new ArrayList<>();

    /** Sum of all item subtotals (before tax). */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Tax amount = subtotal × 19 %. */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /** Discount amount – 0 for now. */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** total = subtotal + taxAmount − discountAmount */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    // ---- Payment fields (populated at checkout) ----

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    /** CASH: amount handed by the customer. */
    @Column(precision = 10, scale = 2)
    private BigDecimal amountReceived;

    /** CASH: change returned to the customer. */
    @Column(precision = 10, scale = 2)
    private BigDecimal changeAmount;

    /** CARD: terminal reference / authorisation code. */
    private String paymentReference;

    /** CREDIT: generated reference number for the credit transaction. */
    private String creditReferenceNumber;

    /** Unique transaction ID generated at checkout completion. */
    private String transactionId;

    /** Timestamp when the sale was completed. */
    private LocalDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---------------------------------------------------------------- helpers

    /** Recalculates subtotal, taxAmount and total from current items. */
    public void recalculateTotals() {
        this.subtotal = items.stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.taxAmount = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        this.total = subtotal.add(taxAmount).subtract(discountAmount);
    }
}
