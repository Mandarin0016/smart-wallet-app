package app.user.service;

import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.property.UserProperties;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final WalletService walletService;
    private final UserProperties userProperties;

    public UserService(PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       SubscriptionService subscriptionService,
                       WalletService walletService, UserProperties userProperties) {

        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.walletService = walletService;
        this.userProperties = userProperties;
    }

    public User login(LoginRequest loginRequest) {

        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return null;
        }

        return user;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());
        if (userOptional.isPresent()) {
            //TODO: throw exception 2 stages:
            // 1. no handling - Spring Fundamentals
            // 2. handling - Spring Advanced
            return null;
        }

        User user = userRepository.save(initializeNewUserAccount(registerRequest));
        Subscription defaultSubscription = subscriptionService.createNewDefaultSubscriptionForNewUser(user);
        Wallet standardWallet = walletService.createNewStandardWalletForNewUser(user);

        user.setSubscription(defaultSubscription);
        ArrayList<Wallet> userWallets = new ArrayList<>();
        userWallets.add(standardWallet);
        user.setWallets(userWallets);

        return userRepository.save(user);
    }

    private User initializeNewUserAccount(RegisterRequest dto) {

        return User.builder()
                .id(UUID.randomUUID())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(userProperties.getDefaultRole())
                .isActive(userProperties.isActiveByDefault())
                .country(dto.getCountry())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public User getById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
