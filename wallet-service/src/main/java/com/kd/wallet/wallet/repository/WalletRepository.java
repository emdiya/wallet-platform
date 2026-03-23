package com.kd.wallet.wallet.repository;

import com.kd.wallet.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

	Optional<Wallet> findFirstByUserIdOrderByCreatedAtAsc(Long userId);

	Optional<Wallet> findFirstByCustomerIdOrderByCreatedAtAsc(String customerId);

	List<Wallet> findByUserIdOrderByCreatedAtAsc(Long userId);

	Optional<Wallet> findByAccountNumber(String accountNumber);

	boolean existsByUserIdAndCurrency(Long userId, String currency);

	boolean existsByAccountNumber(String accountNumber);
}
