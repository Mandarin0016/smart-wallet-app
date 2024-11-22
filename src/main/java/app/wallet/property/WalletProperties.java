package app.wallet.property;

import app.wallet.model.WalletStatus;
import app.wallet.model.WalletType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "domain.wallet.properties")
public class WalletProperties {

    @Valid
    @NotNull
    private Map<WalletType, DefaultWalletProperty> walletToDefaultProperties;
    @NotNull
    private ChronoUnit savingPeriodUnit;
    @Positive
    private int savingPeriodDuration;

    @Data
    public static class DefaultWalletProperty {

        @NotNull
        @Min(0)
        private BigDecimal initialBalance;

        @NotNull
        private WalletStatus defaultStatus;

        @Min(1)
        @Max(Long.MAX_VALUE)
        private long maxTransferCount = Long.MAX_VALUE;
    }
}
