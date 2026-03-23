package com.kd.wallet.wallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_holds")
public class WalletHold {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "hold_id", nullable = false, unique = true, length = 80)
	private String holdId;

	@Column(name = "operation_id", nullable = false, unique = true, length = 80)
	private String operationId;

	@Column(name = "wallet_id", nullable = false)
	private Long walletId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "purpose", length = 255)
	private String purpose;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "processed_at")
	private LocalDateTime processedAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHoldId() {
		return holdId;
	}

	public void setHoldId(String holdId) {
		this.holdId = holdId;
	}

	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public Long getWalletId() {
		return walletId;
	}

	public void setWalletId(Long walletId) {
		this.walletId = walletId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getProcessedAt() {
		return processedAt;
	}

	public void setProcessedAt(LocalDateTime processedAt) {
		this.processedAt = processedAt;
	}
}
