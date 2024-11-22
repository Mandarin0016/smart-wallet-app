package app.subscription.property;

import app.subscription.model.SubscriptionPeriod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "domain.subscription.properties")
public class SubscriptionProperties {

    @NotNull
    private SubscriptionPeriod defaultPeriod;
}
