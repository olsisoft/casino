package com.casino.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_methods", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_stripe_payment_method_id", columnList = "stripePaymentMethodId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodType type;

    @Column(nullable = false)
    private String stripePaymentMethodId;

    @Column(nullable = false)
    private String stripeCustomerId;

    // Card details (for display purposes)
    private String cardBrand; // visa, mastercard, amex, etc.
    private String cardLast4;
    private Integer cardExpMonth;
    private Integer cardExpYear;

    // PayPal details
    private String paypalEmail;

    @Column(nullable = false)
    private Boolean isDefault;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isDefault == null) isDefault = false;
        if (isActive == null) isActive = true;
    }

    public enum PaymentMethodType {
        CARD,
        PAYPAL,
        BANK_TRANSFER
    }
}
