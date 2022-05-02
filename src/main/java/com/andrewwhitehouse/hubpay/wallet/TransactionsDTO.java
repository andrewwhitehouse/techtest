package com.andrewwhitehouse.hubpay.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsDTO {
    String walletId;
    long totalElements;
    int totalPages;
    int pageNumber;
    int numberOfElements;
    List<TransactionDTO> transactions;
}
