package app.transaction.model;

import app.wallet.model.Wallet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Wallet sender;

    @ManyToOne
    private Wallet receiver;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdOn;
}
