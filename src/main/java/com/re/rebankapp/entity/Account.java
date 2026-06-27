package com.re.rebankapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(length = 10)
    private String currency;

    @Column(nullable = false)
    private String transactionPin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "fromAccount", fetch = FetchType.LAZY)
    private List<Transaction> sentTransactions;

    @OneToMany(mappedBy = "toAccount", fetch = FetchType.LAZY)
    private List<Transaction> receivedTransactions;
}
