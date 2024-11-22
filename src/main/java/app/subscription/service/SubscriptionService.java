package app.subscription.service;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.property.SubscriptionProperties;
import app.subscription.repository.SubscriptionRepository;
import app.user.model.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static app.subscription.model.SubscriptionType.DEFAULT;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repository;
    private final SubscriptionProperties properties;

    public SubscriptionService(SubscriptionRepository repository,
                               SubscriptionProperties properties) {

        this.repository = repository;
        this.properties = properties;
    }

    public Subscription createNewDefaultSubscriptionForNewUser(User newUser) {

        Optional<Subscription> optionalSubscription = repository.findByTypeAndOwnerId(DEFAULT, newUser.getId());
        if (optionalSubscription.isPresent()) {
            return optionalSubscription.get();
        }

        Subscription subscription = initializeNewDefaultUserSubscription(newUser);

        return repository.save(subscription);
    }

    private Subscription initializeNewDefaultUserSubscription(User owner) {

        SubscriptionPeriod defaultPeriod = properties.getDefaultPeriod();
        LocalDateTime createdOn = LocalDateTime.now();
        LocalDateTime completedOn = LocalDateTime.now();
        if (defaultPeriod == SubscriptionPeriod.YEARLY) {
            completedOn.plusMonths(12);
        } else {
            completedOn.plusMonths(1);
        }

        return Subscription.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .status(SubscriptionStatus.ACTIVE)
                .period(defaultPeriod)
                .type(DEFAULT)
                .price(BigDecimal.ZERO)
                .renewalAllowed(true)
                .createdOn(createdOn)
                .completedOn(completedOn)
                .terminationReason(null)
                .build();
    }
}
