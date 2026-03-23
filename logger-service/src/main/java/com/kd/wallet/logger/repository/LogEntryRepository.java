package com.kd.wallet.logger.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.wallet.logger.entity.LogEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Repository
public class LogEntryRepository {

	private static final String LOG_FILE_SUFFIX = ".log";
	private static final String MAIN_LOG_FILE_NAME = "main" + LOG_FILE_SUFFIX;

	private final ObjectMapper objectMapper;
	private final Path logDirectory;
	private final AtomicLong nextId;

	public LogEntryRepository(ObjectMapper objectMapper,
			@Value("${logger.file.directory:logs}") String logDirectory) {
		this.objectMapper = objectMapper;
		this.logDirectory = Path.of(logDirectory).toAbsolutePath().normalize();
		this.nextId = new AtomicLong(initializeNextId());
	}

	public synchronized LogEntry save(LogEntry logEntry) {
		ensureDirectoryExists();
		if (logEntry.getId() == null) {
			logEntry.setId(nextId.getAndIncrement());
		}
		if (logEntry.getCreatedAt() == null) {
			logEntry.setCreatedAt(LocalDateTime.now());
		}

		String payload = toJson(logEntry) + System.lineSeparator();
		try {
			writeLogFile(mainLogFile(), payload);
			writeLogFile(serviceLogFile(logEntry.getSourceService()), payload);
			return logEntry;
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to write log files in directory: " + logDirectory, exception);
		}
	}

	public Optional<LogEntry> findById(Long id) {
		return loadAll().stream()
				.filter(entry -> entry.getId().equals(id))
				.findFirst();
	}

	public List<LogEntry> findAllByOrderByCreatedAtDesc() {
		return sortDescending(loadAll());
	}

	public List<LogEntry> findBySourceServiceOrderByCreatedAtDesc(String sourceService) {
		return sortDescending(loadAll().stream()
				.filter(entry -> entry.getSourceService().equals(sourceService))
				.toList());
	}

	public List<LogEntry> findByTraceIdOrderByCreatedAtDesc(String traceId) {
		return sortDescending(loadAll().stream()
				.filter(entry -> traceId.equals(entry.getTraceId()))
				.toList());
	}

	public List<LogEntry> findByHashIdOrderByCreatedAtDesc(String hashId) {
		return sortDescending(loadAll().stream()
				.filter(entry -> hashId.equals(entry.getHashId()))
				.toList());
	}

	private List<LogEntry> loadAll() {
		ensureDirectoryExists();
		Path mainLogFile = mainLogFile();
		if (Files.exists(mainLogFile)) {
			return readEntries(mainLogFile);
		}

		List<LogEntry> entries = new ArrayList<>();
		try (Stream<Path> paths = Files.list(logDirectory)) {
			paths.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(LOG_FILE_SUFFIX))
					.filter(path -> !path.getFileName().toString().equals(MAIN_LOG_FILE_NAME))
					.sorted()
					.forEach(path -> entries.addAll(readEntries(path)));
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to read log directory: " + logDirectory, exception);
		}
		return entries;
	}

	private List<LogEntry> readEntries(Path file) {
		try {
			List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
			List<LogEntry> entries = new ArrayList<>();
			for (String line : lines) {
				if (line == null || line.isBlank()) {
					continue;
				}
				entries.add(objectMapper.readValue(line, LogEntry.class));
			}
			return entries;
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to read log file: " + file, exception);
		}
	}

	private List<LogEntry> sortDescending(List<LogEntry> entries) {
		return entries.stream()
				.sorted(Comparator.comparing(LogEntry::getCreatedAt).reversed().thenComparing(LogEntry::getId).reversed())
				.toList();
	}

	private long initializeNextId() {
		return loadAll().stream()
				.map(LogEntry::getId)
				.filter(id -> id != null)
				.max(Long::compareTo)
				.map(id -> id + 1)
				.orElse(1L);
	}

	private void ensureDirectoryExists() {
		try {
			Files.createDirectories(logDirectory);
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to create log directory: " + logDirectory, exception);
		}
	}

	private Path mainLogFile() {
		return logDirectory.resolve(MAIN_LOG_FILE_NAME);
	}

	private Path serviceLogFile(String sourceService) {
		return logDirectory.resolve(sourceService + LOG_FILE_SUFFIX);
	}

	private void writeLogFile(Path logFile, String payload) throws IOException {
		Files.writeString(
				logFile,
				payload,
				StandardCharsets.UTF_8,
				StandardOpenOption.CREATE,
				StandardOpenOption.WRITE,
				StandardOpenOption.APPEND
		);
	}

	private String toJson(LogEntry logEntry) {
		try {
			return objectMapper.writeValueAsString(logEntry);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize log entry", exception);
		}
	}
}
