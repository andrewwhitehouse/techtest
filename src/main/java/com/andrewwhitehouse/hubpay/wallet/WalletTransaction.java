package com.andrewwhitehouse.hubpay.wallet;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "TRANSACTIONS")
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @NonNull
    private String walletId;
    @NonNull
    private Integer amountPence;
    private OffsetDateTime created;
}