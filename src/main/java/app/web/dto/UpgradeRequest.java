package app.web.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UpgradeRequest {

    private String subscriptionType;

    private String subscriptionPeriod;

    private String walletId;
}
