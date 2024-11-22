package app.wallet.repository;

import app.wallet.model.Wallet;
import app.wallet.model.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    List<Wallet> findAllByTypeAndOwnerIdOrderByCreatedOn(WalletType type, UUID ownerId);
}
