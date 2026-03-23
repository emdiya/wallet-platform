package com.kd.wallet.wallet.repository;

import com.kd.wallet.wallet.entity.WalletHold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletHoldRepository extends JpaRepository<WalletHold, Long> {

	Optional<WalletHold> findByHoldId(String holdId);
}
