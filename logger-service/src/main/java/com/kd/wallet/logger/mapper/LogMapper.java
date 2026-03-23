package com.kd.wallet.logger.mapper;

import com.kd.wallet.logger.dto.request.CreateLogRequest;
import com.kd.wallet.logger.dto.response.LogResponse;
import com.kd.wallet.logger.entity.LogEntry;
import org.springframework.stereotype.Component;

@Component
public class LogMapper {

	public LogEntry toEntity(CreateLogRequest request,
			String normalizedServiceName,
			String normalizedLevel,
			String normalizedTraceId,
			String normalizedHashId) {
		LogEntry logEntry = new LogEntry();
		logEntry.setSourceService(normalizedServiceName);
		logEntry.setLevel(normalizedLevel);
		logEntry.setMessage(request.message().trim());
		logEntry.setTraceId(normalizedTraceId);
		logEntry.setHashId(normalizedHashId);
		logEntry.setDetails(normalizeNullable(request.details()));
		return logEntry;
	}

	public LogResponse toResponse(LogEntry logEntry) {
		return new LogResponse(
				logEntry.getId(),
				logEntry.getSourceService(),
					logEntry.getLevel(),
					logEntry.getMessage(),
					logEntry.getTraceId(),
					logEntry.getHashId(),
					logEntry.getDetails(),
					logEntry.getCreatedAt()
			);
	}

	private String normalizeNullable(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

}
