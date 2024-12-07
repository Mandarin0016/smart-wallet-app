package app.web.mapper;

import app.subscription.model.Subscription;
import app.transaction.model.Transaction;
import app.user.model.User;
import app.web.dto.SubscriptionHistory;
import app.web.dto.TransactionResult;
import app.web.dto.UserInformation;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static TransactionResult toTransactionResult(Transaction transaction) {

        return TransactionResult.builder()
                .id(transaction.getId())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .balanceLeft(transaction.getBalanceLeft())
                .currency(transaction.getCurrency())
                .type(transaction.getType())
                .failureReason(transaction.getFailureReason())
                .createdOn(transaction.getCreatedOn())
                .build();
    }

    public static SubscriptionHistory toSubscriptionHistory(Subscription subscription) {

        return SubscriptionHistory.builder()
                .id(subscription.getId())
                .type(subscription.getType())
                .status(subscription.getStatus())
                .period(subscription.getPeriod())
                .price(subscription.getPrice())
                .start(subscription.getCreatedOn())
                .end(subscription.getCompletedOn())
                .build();
    }

    public static UserInformation toUserInformation(User user) {

        return UserInformation.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .country(user.getCountry())
                .isActive(user.isActive())
                .createdOn(user.getCreatedOn())
                .build();
    }
}
