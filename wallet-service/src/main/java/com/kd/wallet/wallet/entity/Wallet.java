package com.kd.wallet.wallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
public class Wallet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "customer_id", nullable = false, length = 32)
	private String customerId;

	@Column(name = "account_name", nullable = false, length = 120)
	private String accountName;

	@Column(name = "account_number", nullable = false, unique = true, length = 20)
	private String accountNumber;

	@Column(name = "currency", nullable = false, length = 10)
	private String currency;

	@Column(name = "balance", nullable = false, precision = 19, scale = 2)
	private BigDecimal balance;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (balance == null) {
			balance = BigDecimal.ZERO;
		}
		if (currency == null || currency.isBlank()) {
			currency = "USD";
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
