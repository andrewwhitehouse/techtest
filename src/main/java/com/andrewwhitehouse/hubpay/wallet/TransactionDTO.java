package com.andrewwhitehouse.hubpay.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    OffsetDateTime created;
    int amountPence;
}
