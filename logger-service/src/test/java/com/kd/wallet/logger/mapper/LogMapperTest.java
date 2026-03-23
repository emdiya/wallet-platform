package com.kd.wallet.logger.mapper;

import com.kd.wallet.logger.dto.request.CreateLogRequest;
import com.kd.wallet.logger.dto.response.LogResponse;
import com.kd.wallet.logger.entity.LogEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogMapperTest {

	private final LogMapper logMapper = new LogMapper();

	@Test
	void shouldMapRequestToEntity() {
		CreateLogRequest request = new CreateLogRequest(
				"wallet-service",
				"info",
				" Wallet created ",
				"trace-123",
				"abcdef1234567890",
				" details "
		);

		LogEntry logEntry = logMapper.toEntity(request, "wallet-service", "INFO", "trace-123", "abcdef1234567890");

		assertEquals("wallet-service", logEntry.getSourceService());
		assertEquals("INFO", logEntry.getLevel());
		assertEquals("Wallet created", logEntry.getMessage());
		assertEquals("trace-123", logEntry.getTraceId());
		assertEquals("abcdef1234567890", logEntry.getHashId());
		assertEquals("details", logEntry.getDetails());
	}

	@Test
	void shouldMapEntityToResponse() {
		LogEntry logEntry = new LogEntry();
		logEntry.setId(3L);
		logEntry.setSourceService("auth-service");
		logEntry.setLevel("ERROR");
		logEntry.setMessage("Login failed");
		logEntry.setTraceId("trace-456");
		logEntry.setHashId("abcdef1234567890");
		logEntry.setDetails("Invalid password");
		logEntry.setCreatedAt(LocalDateTime.of(2026, 3, 19, 11, 30));

		LogResponse response = logMapper.toResponse(logEntry);

		assertEquals(3L, response.id());
		assertEquals("auth-service", response.sourceService());
		assertEquals("ERROR", response.level());
		assertEquals("abcdef1234567890", response.hashId());
	}

}
