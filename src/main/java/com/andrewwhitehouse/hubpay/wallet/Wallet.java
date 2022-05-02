package com.andrewwhitehouse.hubpay.wallet;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "WALLETS")
public class Wallet {
    @Id @NonNull
    private String id;
    @NonNull
    private String customerId;
    @NonNull
    private Integer balancePence;
}
