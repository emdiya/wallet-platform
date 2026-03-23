package com.kd.wallet.transfer.repository;

import com.kd.wallet.transfer.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

	Optional<Transfer> findByRequestId(String requestId);

	Optional<Transfer> findByReferenceNo(String referenceNo);
}
