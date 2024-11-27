package app.wallet.service;

import app.exception.DomainException;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.property.WalletProperties;
import app.wallet.repository.WalletRepository;

import java.util.Currency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static app.wallet.model.WalletType.STANDARD;

@Slf4j
@Service
public class WalletService {

    private final WalletRepository repository;
    private final WalletProperties properties;
    private final TransactionService transactionService;
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository repository,
                         WalletProperties properties,
                         TransactionService transactionService, WalletRepository walletRepository) {

        this.repository = repository;
        this.properties = properties;
        this.transactionService = transactionService;
        this.walletRepository = walletRepository;
    }

    public Wallet createNewStandardWalletForNewUser(User newUser) {

        List<Wallet> wallets = repository.findAllByTypeAndOwnerIdOrderByCreatedOn(STANDARD, newUser.getId());
        if (!wallets.isEmpty()) {
            return wallets.get(0);
        }

        Wallet standardWallet = initializeNewStandardWallet(newUser);
        log.info("Successfully created new wallet with id [%s] and type [%s] for user with id [%s]".formatted(
                standardWallet.getId(), standardWallet.getType(), newUser.getId()));

        return repository.save(standardWallet);
    }

    private Wallet initializeNewStandardWallet(User owner) {

        WalletProperties.DefaultWalletProperty standardWalletProperties = properties.getWalletToDefaultProperties().get(STANDARD);

        return Wallet.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .currency(Currency.getInstance("EUR"))
                .type(STANDARD)
                .status(standardWalletProperties.getDefaultStatus())
                .balance(standardWalletProperties.getInitialBalance())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public Transaction charge(User user, UUID walletId, BigDecimal amount, String chargeDescription) {

        if (!isWalletAssociatedWithUser(user, walletId)) {
            String message = "User with id [%s] is not associated with wallet with id [%s]".formatted(user.getId(), walletId);
            throw new DomainException(message, HttpStatus.FORBIDDEN);
        }

        Wallet wallet = user.getWallets().stream()
                .filter(w -> w.getId().equals(walletId))
                .findFirst()
                .get();

        if (wallet.getBalance().compareTo(amount) < 0) {
            String failureReason = "Insufficient funds, top up your account";
            return transactionService.createNewTransaction(user, walletId.toString(), "Smart Wallet Ltd", amount, wallet.getBalance(), wallet.getCurrency(), TransactionStatus.FAILED, TransactionType.WITHDRAWAL, chargeDescription, failureReason);
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);

        return transactionService.createNewTransaction(user, walletId.toString(), "Smart Wallet Ltd", amount, newBalance, wallet.getCurrency(), TransactionStatus.SUCCEEDED, TransactionType.WITHDRAWAL, chargeDescription, null);
    }

    private boolean isWalletAssociatedWithUser(User user, UUID walletId) {

        return user.getWallets().stream().map(Wallet::getId).anyMatch(walletId::equals);
    }
}
