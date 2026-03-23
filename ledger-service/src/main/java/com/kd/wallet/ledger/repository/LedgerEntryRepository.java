package com.kd.wallet.ledger.repository;

import com.kd.wallet.ledger.entity.LedgerEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

	boolean existsByEventId(String eventId);

	Optional<LedgerEntry> findByEventId(String eventId);

	@Query("""
			select le from LedgerEntry le
			where (:requestId is null or le.requestId = :requestId)
			  and (:accountNumber is null or le.accountNumber = :accountNumber
			       or le.fromAccountNumber = :accountNumber
			       or le.toAccountNumber = :accountNumber)
			  and (:eventType is null or le.eventType = :eventType)
			  and (:sourceTopic is null or le.sourceTopic = :sourceTopic)
			  and (:holdId is null or le.holdId = :holdId)
			order by le.occurredAt desc, le.id desc
			""")
	List<LedgerEntry> search(@Param("requestId") String requestId,
			@Param("accountNumber") String accountNumber,
			@Param("eventType") String eventType,
			@Param("sourceTopic") String sourceTopic,
			@Param("holdId") String holdId,
			Pageable pageable);
}
