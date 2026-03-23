package com.kd.wallet.wallet.repository;

import com.kd.wallet.wallet.entity.WalletOperation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletOperationRepository extends JpaRepository<WalletOperation, Long> {

	boolean existsByOperationId(String operationId);
}
