package com.kd.wallet.logger.service;

import com.kd.wallet.logger.dto.request.CreateLogRequest;
import com.kd.wallet.logger.dto.response.LogResponse;
import java.util.List;

public interface LogService {

	LogResponse create(CreateLogRequest request);

	LogResponse getById(Long id);

	List<LogResponse> getAll();

	List<LogResponse> getBySourceService(String sourceService);

	List<LogResponse> getByTraceId(String traceId);

	List<LogResponse> getByHashId(String hashId);

}
