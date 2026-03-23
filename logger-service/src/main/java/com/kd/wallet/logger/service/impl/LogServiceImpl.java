package com.kd.wallet.logger.service.impl;

import com.kd.wallet.logger.dto.request.CreateLogRequest;
import com.kd.wallet.logger.dto.response.LogResponse;
import com.kd.wallet.logger.entity.LogEntry;
import com.kd.wallet.logger.exception.ResourceNotFoundException;
import com.kd.wallet.logger.mapper.LogMapper;
import com.kd.wallet.logger.repository.LogEntryRepository;
import com.kd.wallet.logger.service.LogService;
import com.kd.wallet.logger.util.HashIdUtils;
import com.kd.wallet.logger.util.LogLevelUtils;
import com.kd.wallet.logger.util.ServiceNameUtils;
import com.kd.wallet.logger.util.TraceIdUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {

	private final LogEntryRepository logEntryRepository;
	private final LogMapper logMapper;

	public LogServiceImpl(LogEntryRepository logEntryRepository, LogMapper logMapper) {
		this.logEntryRepository = logEntryRepository;
		this.logMapper = logMapper;
	}

	@Override
	public LogResponse create(CreateLogRequest request) {
		String sourceService = ServiceNameUtils.normalize(request.sourceService());
		String level = LogLevelUtils.normalize(request.level());
		String traceId = TraceIdUtils.normalizeNullable(request.traceId());
		String hashId = HashIdUtils.normalizeNullable(request.hashId());

		LogEntry logEntry = logMapper.toEntity(request, sourceService, level, traceId, hashId);
		return logMapper.toResponse(logEntryRepository.save(logEntry));
	}

	@Override
	public LogResponse getById(Long id) {
		LogEntry logEntry = logEntryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Log not found with id: " + id));
		return logMapper.toResponse(logEntry);
	}

	@Override
	public List<LogResponse> getAll() {
		return logEntryRepository.findAllByOrderByCreatedAtDesc()
				.stream()
				.map(logMapper::toResponse)
				.toList();
	}

	@Override
	public List<LogResponse> getBySourceService(String sourceService) {
		String normalizedServiceName = ServiceNameUtils.normalize(sourceService);
		return logEntryRepository.findBySourceServiceOrderByCreatedAtDesc(normalizedServiceName)
				.stream()
				.map(logMapper::toResponse)
				.toList();
	}

	@Override
	public List<LogResponse> getByTraceId(String traceId) {
		String normalizedTraceId = TraceIdUtils.normalizeRequired(traceId);
		return logEntryRepository.findByTraceIdOrderByCreatedAtDesc(normalizedTraceId)
				.stream()
				.map(logMapper::toResponse)
				.toList();
	}

	@Override
	public List<LogResponse> getByHashId(String hashId) {
		String normalizedHashId = HashIdUtils.normalizeRequired(hashId);
		return logEntryRepository.findByHashIdOrderByCreatedAtDesc(normalizedHashId)
				.stream()
				.map(logMapper::toResponse)
				.toList();
	}

}
