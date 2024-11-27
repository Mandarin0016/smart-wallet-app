package app.transaction.repository;

import app.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Ivan -> Gosho (20 $):
    // 1. Transaction (WITHDRAWAL for Ivan -20 $)
    // 2. Transaction (DEPOSIT for Gosho +20 $)
    List<Transaction> findAllByOwnerIdOrderByCreatedOnDesc(UUID ownerId);

}
