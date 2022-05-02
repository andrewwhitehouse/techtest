package com.andrewwhitehouse.hubpay.wallet;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends CrudRepository<Wallet, String> {

    @Modifying
    @Query("UPDATE Wallet w SET w.balancePence = w.balancePence + :amount WHERE w.id = :id")
    int adjustWalletBalance(@Param("id") String walletId,
                             @Param("amount") Integer adjustmentPence);
}
