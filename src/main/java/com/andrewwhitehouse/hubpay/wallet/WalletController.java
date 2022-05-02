package com.andrewwhitehouse.hubpay.wallet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/wallets")
public class WalletController {

    public static final int DEFAULT_PAGE_SIZE = 5;
    public static final int DEFAULT_PAGE_NUMBER = 1;

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> create(@RequestBody CreateWalletDTO createWalletDTO) {
        return ResponseEntity.ok(walletService.create(createWalletDTO.getCustomerId()));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<Wallet> retrieve(@PathVariable("walletId") String walletId) {
        return ResponseEntity.ok(walletService.findById(walletId));
    }

    @PostMapping("/{walletId}/add-funds")
    public ResponseEntity<Void> addFunds(@PathVariable("walletId") String walletId,
                                           @RequestBody BalanceAdjustmentDTO balanceAdjustment) {
        walletService.addFunds(walletId, balanceAdjustment.getAmountPence());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{walletId}/withdraw-funds")
    public ResponseEntity<Void> withdrawFunds(@PathVariable("walletId") String walletId,
                                           @RequestBody BalanceAdjustmentDTO balanceAdjustment) {
        walletService.withdraw(walletId, balanceAdjustment.getAmountPence());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<TransactionsDTO> transactions(@PathVariable("walletId") String walletId,
                                                        @RequestParam(name = "page", defaultValue = "1") Integer pageNumber,
                                                        @RequestParam(name = "size", defaultValue = "5") Integer pageSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Minimum page number is 1");
        }
        return ResponseEntity.ok(walletService.getTransactions(walletId, pageNumber, pageSize));
    }
}
