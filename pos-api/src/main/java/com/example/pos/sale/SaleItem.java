package com.example.pos.sale;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Back-reference to the parent sale. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    @ToString.Exclude
    private Sale sale;

    // ---- Product snapshot (denormalised) ----

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String barcode;

    /** Price at the moment the item was added. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    /** unitPrice × quantity */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // ---- Computed helper ----

    public void recalculate() {
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
