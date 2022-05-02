package com.andrewwhitehouse.hubpay.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletDTO {
    String customerId;
}
