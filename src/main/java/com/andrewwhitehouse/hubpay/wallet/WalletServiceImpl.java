package com.andrewwhitehouse.hubpay.wallet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    private static final int MINIMUM_ADD_PENCE = 10 * 100;
    private static final int MAXIMUM_ADD_PENCE = 10000 * 100;

    private final WalletRepository walletRepository;

    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public Wallet findById(String walletId) {
        return walletRepository.findById(walletId).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Wallet create(String customerId) {
        Wallet wallet = new Wallet(UUID.randomUUID().toString(),
                customerId, 0);
        walletRepository.save(wallet);
        return wallet;
    }

    @Override
    public void addFunds(String walletId, int amountPence) {
        if (amountPence < MINIMUM_ADD_PENCE) {
            throw new IllegalArgumentException("Amount is below minimum");
        }
        if (amountPence > MAXIMUM_ADD_PENCE) {
            throw new IllegalArgumentException("Amount is above maximum");
        }
        int updated = walletRepository.adjustWalletBalance(walletId, amountPence);
        if (updated == 0) {
            throw new ResourceNotFoundException();
        }
        WalletTransaction txn = WalletTransaction.builder()
                .walletId(walletId)
                .created(OffsetDateTime.now())
                .amountPence(amountPence)
                .build();
        walletTransactionRepository.save(txn);
    }

    @Override
    public void withdraw(String walletId, int amountPence) {
        if (amountPence > MAXIMUM_ADD_PENCE) {
            throw new IllegalArgumentException("Amount is above withdrawal maximum");
        }
        if (amountPence < 0) {
            throw new IllegalArgumentException("Cannot withdraw negative amount");
        }
        Wallet wallet = findById(walletId);
        if (wallet.getBalancePence() < amountPence) {
            throw new IllegalArgumentException("Withdrawal amount must not exceed balance");
        }
        int updated = walletRepository.adjustWalletBalance(walletId, -amountPence);
        if (updated == 0) {
            throw new IllegalStateException(String.format("adjustWalletBalance failed walletId {} amount {}",
                    walletId, amountPence));
        }
        WalletTransaction txn = WalletTransaction.builder()
                .walletId(walletId)
                .created(OffsetDateTime.now())
                .amountPence(-amountPence)
                .build();
        walletTransactionRepository.save(txn);
    }

    @Override
    public TransactionsDTO getTransactions(String walletId, int pageNumber, int size) {
        // JPA paging starts from 0
        Pageable paging = PageRequest.of(pageNumber-1, size);
        Page result = walletTransactionRepository.findAllByWalletId(walletId, paging);
        return toDTO(walletId, result);
    }

    private TransactionsDTO toDTO(String walletId, Page<WalletTransaction> page) {
        return TransactionsDTO.builder()
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .walletId(walletId)
                .numberOfElements(page.getNumberOfElements())
                .pageNumber(page.getNumber()+1)
                .walletId(walletId)
                .transactions(page.getContent().stream().map(this::toDTO).collect(Collectors.toList()))
                .build();
    }

    private TransactionDTO toDTO(WalletTransaction txn) {
        return TransactionDTO.builder()
                .created(txn.getCreated())
                .amountPence(txn.getAmountPence())
                .build();
    }
}
