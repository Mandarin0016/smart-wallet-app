package app.subscription.service;

import app.exception.DomainException;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.property.SubscriptionProperties;
import app.subscription.repository.SubscriptionRepository;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.user.model.User;
import app.wallet.service.WalletService;
import app.web.dto.UpgradeOption;
import app.web.dto.UpgradeRequest;
import app.web.dto.UpgradeResult;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static app.subscription.model.SubscriptionType.*;

@Service
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository repository;
    private final SubscriptionProperties properties;
    private final WalletService walletService;

    public SubscriptionService(SubscriptionRepository repository,
                               SubscriptionProperties properties,
                               WalletService walletService) {

        this.repository = repository;
        this.properties = properties;
        this.walletService = walletService;
    }

    public Subscription createNewDefaultSubscriptionForNewUser(User newUser) {

        Optional<Subscription> optionalSubscription = repository.findByTypeAndOwnerId(DEFAULT, newUser.getId());
        if (optionalSubscription.isPresent()) {
            return optionalSubscription.get();
        }

        Subscription subscription = buildNewUserSubscription(newUser, DEFAULT, properties.getDefaultPeriod(), BigDecimal.ZERO);
        log.info("Successfully created new [%s] subscription with id [%s].".formatted(subscription.getType(), subscription.getId()));

        return repository.save(subscription);
    }

    private Subscription buildNewUserSubscription(User owner, SubscriptionType type, SubscriptionPeriod period, BigDecimal price) {

        LocalDateTime createdOn = LocalDateTime.now();
        LocalDateTime completedOn = LocalDateTime.now();
        if (period == SubscriptionPeriod.YEARLY) {
            completedOn = completedOn.plusMonths(12);
        } else {
            completedOn = completedOn.plusMonths(1);
        }

        return Subscription.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .status(SubscriptionStatus.ACTIVE)
                .period(period)
                .type(type)
                .price(price)
                .renewalAllowed(period == SubscriptionPeriod.MONTHLY)
                .createdOn(createdOn)
                .completedOn(completedOn)
                .build();
    }

    public Map<String, UpgradeOption> getUpgradeOptions(UUID userId) {

        Subscription currentUserSubscription = getCurrentActiveSubscription(userId);

        UpgradeOption defaultSubscriptionOption = getOptionByType(DEFAULT);
        UpgradeOption premiumSubscriptionOption = getOptionByType(PREMIUM);
        UpgradeOption ultimateSubscriptionOption = getOptionByType(ULTIMATE);

        switch (currentUserSubscription.getType()) {
            case DEFAULT -> defaultSubscriptionOption.setChoosable(false);
            case PREMIUM -> premiumSubscriptionOption.setChoosable(false);
            case ULTIMATE -> ultimateSubscriptionOption.setChoosable(false);
        }

        return Map.of(
                DEFAULT.name(), defaultSubscriptionOption,
                PREMIUM.name(), premiumSubscriptionOption,
                ULTIMATE.name(), ultimateSubscriptionOption
        );

    }

    @Transactional
    public UpgradeResult upgrade(User user, UpgradeRequest request) {

        Subscription userSubscription = getCurrentActiveSubscription(user.getId());
        rejectSameSubscriptionUpgradeRequest(userSubscription, request);

        SubscriptionType desiredSubscriptionType = SubscriptionType.valueOf(request.getSubscriptionType().toUpperCase());
        SubscriptionPeriod desiredSubscriptionPeriod = SubscriptionPeriod.valueOf(request.getSubscriptionPeriod().toUpperCase());

        BigDecimal price;
        if (desiredSubscriptionPeriod == SubscriptionPeriod.MONTHLY) {
            price = properties.getUpgradeOptions().get(desiredSubscriptionType).getMonthlyPrice();
        } else {
            price = properties.getUpgradeOptions().get(desiredSubscriptionType).getYearlyPrice();
        }

        String chargeDescription = "Purchased %s subscription".formatted(desiredSubscriptionType);
        Transaction chargeResult = walletService.charge(user, UUID.fromString(request.getWalletId()), price, chargeDescription);

        if (chargeResult.getStatus() == TransactionStatus.FAILED) {

            log.warn("Upgrade attempt failed because of [%s].".formatted(chargeResult.getFailureReason()));

            return UpgradeResult.builder()
                    .transaction(chargeResult)
                    .oldSubscription(userSubscription)
                    .newSubscription(userSubscription)
                    .build();
        }

        Subscription newUserSubscription = buildNewUserSubscription(user, desiredSubscriptionType, desiredSubscriptionPeriod, price);
        userSubscription.setCompletedOn(LocalDateTime.now());
        userSubscription.setStatus(SubscriptionStatus.COMPLETED);

        repository.save(userSubscription);
        repository.save(newUserSubscription);

        return UpgradeResult.builder()
                .transaction(chargeResult)
                .oldSubscription(userSubscription)
                .newSubscription(newUserSubscription)
                .build();
    }

    private void rejectSameSubscriptionUpgradeRequest(Subscription currentSubscription, UpgradeRequest upgradeRequest) {

        boolean isSameType = currentSubscription.getType() == SubscriptionType.valueOf(upgradeRequest.getSubscriptionType().toUpperCase());
        boolean isSamePeriod = currentSubscription.getPeriod() == SubscriptionPeriod.valueOf(upgradeRequest.getSubscriptionPeriod().toUpperCase());

        if (isSameType && isSamePeriod) {
            String message = "User with id [%s] is already on subscription of type [%s] and period [%s]".formatted(currentSubscription.getOwner().getId(), currentSubscription.getType(), currentSubscription.getPeriod());
            throw new DomainException(message, HttpStatus.BAD_REQUEST);
        }
    }

    private Subscription getCurrentActiveSubscription(UUID userId) {

        return repository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, userId)
                .orElseThrow(() -> new DomainException("User with id [%s] doesn't have any active subscription.".formatted(userId), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private UpgradeOption getOptionByType(SubscriptionType type) {

        return UpgradeOption.builder()
                .type(type)
                .benefits(properties.getUpgradeOptions().get(type).getBenefits())
                .monthlyPrice(properties.getUpgradeOptions().get(type).getMonthlyPrice())
                .yearlyPrice(properties.getUpgradeOptions().get(type).getYearlyPrice())
                .isChoosable(true)
                .build();
    }

    public List<Subscription> getHistory(UUID userId) {

        return repository.findByOwnerIdOrderByCreatedOnDesc(userId);
    }

    public List<Subscription> getAllSubscriptions() {

        return repository.findAll();
    }
}
