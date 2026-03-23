package com.kd.wallet.logger.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kd.wallet.logger.entity.LogEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogEntryRepositoryTest {

	@TempDir
	Path tempDir;

	@Test
	void shouldPersistTraceAndDebugLogsToMainAndServiceFiles() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		LogEntryRepository repository = new LogEntryRepository(objectMapper, tempDir.toString());

		LogEntry traceLog = createLogEntry("wallet-service", "TRACE", "Trace message");
		LogEntry debugLog = createLogEntry("wallet-service", "DEBUG", "Debug message");

		repository.save(traceLog);
		repository.save(debugLog);

		Path mainLog = tempDir.resolve("main.log");
		Path serviceLog = tempDir.resolve("wallet-service.log");

		assertTrue(Files.exists(mainLog));
		assertTrue(Files.exists(serviceLog));

		List<String> mainLines = Files.readAllLines(mainLog);
		List<String> serviceLines = Files.readAllLines(serviceLog);

		assertEquals(2, mainLines.size());
		assertEquals(2, serviceLines.size());
		assertTrue(mainLines.stream().anyMatch(line -> line.contains("\"level\":\"TRACE\"")));
		assertTrue(mainLines.stream().anyMatch(line -> line.contains("\"level\":\"DEBUG\"")));
		assertTrue(serviceLines.stream().anyMatch(line -> line.contains("\"message\":\"Trace message\"")));
		assertTrue(serviceLines.stream().anyMatch(line -> line.contains("\"message\":\"Debug message\"")));
	}

	private LogEntry createLogEntry(String sourceService, String level, String message) {
		LogEntry logEntry = new LogEntry();
		logEntry.setSourceService(sourceService);
		logEntry.setLevel(level);
		logEntry.setMessage(message);
		logEntry.setTraceId("trace-123");
		logEntry.setHashId("abcdef1234567890");
		logEntry.setDetails("details");
		logEntry.setCreatedAt(LocalDateTime.of(2026, 3, 20, 10, 45));
		return logEntry;
	}
}
