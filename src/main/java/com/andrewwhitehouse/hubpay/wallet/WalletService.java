package com.andrewwhitehouse.hubpay.wallet;

import java.util.List;
import java.util.Optional;

public interface WalletService {
    Wallet create(String customerId);
    Wallet findById(String walletId);
    void addFunds(String walletId, int amountPence);
    void withdraw(String walletId, int amountPence);
    TransactionsDTO getTransactions(String walletId, int pageNumber, int size);
}
