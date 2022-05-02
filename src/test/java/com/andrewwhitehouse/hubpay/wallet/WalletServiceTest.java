package com.andrewwhitehouse.hubpay.wallet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    final String WALLET_ID = "1234";
    final String CUSTOMER_ID = "customer1";
    final int WALLET_BALANCE = 5000;

    private WalletService walletService;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Captor
    private ArgumentCaptor<WalletTransaction> transactionCaptor;

    @BeforeEach
    void setUp() {
        walletService = new WalletServiceImpl(walletRepository, walletTransactionRepository);
    }

    @Test
    void shouldRetrieveWallet() {
        when(walletRepository.findById(WALLET_ID)).thenReturn(
                Optional.of(new Wallet(WALLET_ID, CUSTOMER_ID, WALLET_BALANCE+1)));
        Wallet wallet = walletService.findById(WALLET_ID);
        assertThat(wallet.getId()).isEqualTo(WALLET_ID);
        assertThat(wallet.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(wallet.getBalancePence()).isEqualTo(WALLET_BALANCE+1);
    }

    @Test
    void shouldSaveNewWallet() {
        Wallet wallet = walletService.create(CUSTOMER_ID);
        assertNotNull(wallet.getId());
        assertThat(wallet.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(wallet.getBalancePence()).isEqualTo(0);
        verify(walletRepository).save(wallet);
    }

    @Test
    void addFundsShouldIncreaseBalance() {
        final int AMOUNT = 2000;
        when(walletRepository.adjustWalletBalance(WALLET_ID, AMOUNT)).thenReturn(1);

        walletService.addFunds(WALLET_ID, AMOUNT);

        Mockito.verify(walletTransactionRepository).save(transactionCaptor.capture());
        WalletTransaction txn = transactionCaptor.getValue();
        assertThat(txn.getAmountPence()).isEqualTo(AMOUNT);
        assertThat(txn.getWalletId()).isEqualTo(WALLET_ID);
    }

    @Test
    void withdrawFundsShouldReduceBalance() {
        final int AMOUNT = 2000;
        when(walletRepository.findById(WALLET_ID))
                .thenReturn(Optional.of(new Wallet(WALLET_ID, "", 3000)));
        when(walletRepository.adjustWalletBalance(WALLET_ID, -AMOUNT)).thenReturn(1);

        walletService.withdraw(WALLET_ID, 2000);

        Mockito.verify(walletTransactionRepository).save(transactionCaptor.capture());
        WalletTransaction txn = transactionCaptor.getValue();
        assertThat(txn.getAmountPence()).isEqualTo(-AMOUNT);
        assertThat(txn.getWalletId()).isEqualTo(WALLET_ID);
    }

    @Test
    void cannotAddLessThanTenPounds() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            walletService.addFunds("", 999);
        });
        assertThat(thrown.getMessage()).isEqualTo("Amount is below minimum");
    }

    @Test
    void cannotAddMoreThanTenThousandPounds() {
        final int TEN_THOUSAND_POUNDS_IN_PENCE = 10000 * 100;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            walletService.addFunds("",
                    TEN_THOUSAND_POUNDS_IN_PENCE + 1);
        });
        assertThat(thrown.getMessage()).isEqualTo("Amount is above maximum");
    }

    @Test
    void cannotWithdrawNegativeAmount() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            walletService.withdraw("", -1);
        });
        assertThat(thrown.getMessage()).isEqualTo("Cannot withdraw negative amount");
    }

    @Test
    void cannotWithdrawMoreThan10000Pounds() {
        final int TEN_THOUSAND_POUNDS_IN_PENCE = 10000 * 100;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            walletService.withdraw("", TEN_THOUSAND_POUNDS_IN_PENCE + 1);
        });
        assertThat(thrown.getMessage()).isEqualTo("Amount is above withdrawal maximum");
    }

    @Test
    void withdrawalMustNotBeGreaterThanBalance() {
        when(walletRepository.findById(WALLET_ID))
                .thenReturn(Optional.of(new Wallet(WALLET_ID, CUSTOMER_ID, WALLET_BALANCE)));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            walletService.withdraw(WALLET_ID, WALLET_BALANCE + 1);
        });
        assertThat(thrown.getMessage()).isEqualTo("Withdrawal amount must not exceed balance");
    }

    @Test
    void shouldReturnNotFoundIfAddFails() {
        when(walletRepository.adjustWalletBalance(WALLET_ID, 1000))
                .thenReturn(0);
        assertThrows(ResourceNotFoundException.class, () -> {
            walletService.addFunds(WALLET_ID, 1000);
        });
    }

    @Test
    void withdrawShouldReturnNotFoundForNonexistentWallet() {
        assertThrows(ResourceNotFoundException.class, () -> {
            walletService.withdraw(WALLET_ID, 1000);
        });
    }

    @Test
    void withdrawShouldReturnExceptionIfUpdateFails() {
        when(walletRepository.findById(WALLET_ID))
                .thenReturn(Optional.of(new Wallet(WALLET_ID, CUSTOMER_ID, 10000)));
        when(walletRepository.adjustWalletBalance(WALLET_ID, -2000)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> {
            walletService.withdraw(WALLET_ID, 2000);
        });
    }

    @Test
    void shouldProvideCorrectPagingParamaters() {
        final int PAGE_NUMBER = 1;
        final int PAGE_SIZE = 7;
        Pageable paging = PageRequest.of(PAGE_NUMBER-1, PAGE_SIZE);
        when(walletTransactionRepository.findAllByWalletId(WALLET_ID, paging))
                .thenReturn(Page.empty());
        TransactionsDTO transactions = walletService.getTransactions(WALLET_ID, 1, 7);
        assertThat(transactions.numberOfElements).isZero();
    }
}
