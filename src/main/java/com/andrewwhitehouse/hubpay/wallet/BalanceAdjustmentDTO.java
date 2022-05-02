package com.andrewwhitehouse.hubpay.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceAdjustmentDTO {
   int amountPence;
}
