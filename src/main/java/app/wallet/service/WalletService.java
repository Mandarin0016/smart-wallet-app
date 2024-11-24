package app.wallet.service;

import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.property.WalletProperties;
import app.wallet.repository.WalletRepository;
import java.util.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
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

    public WalletService(WalletRepository repository, WalletProperties properties) {

        this.repository = repository;
        this.properties = properties;
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
                .currentTransferCount(0)
                .maxTransferCount(standardWalletProperties.getMaxTransferCount())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}
