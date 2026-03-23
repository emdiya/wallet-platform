package com.kd.wallet.logger.controller;

import com.kd.wallet.logger.dto.request.CreateLogRequest;
import com.kd.wallet.logger.dto.response.LogResponse;
import com.kd.wallet.logger.service.LogService;
import com.kd.wallet.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

	private final LogService logService;

	public LogController(LogService logService) {
		this.logService = logService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<LogResponse>> createLog(@Valid @RequestBody CreateLogRequest request) {
		LogResponse response = logService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Log created successfully", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<LogResponse>>> getAllLogs() {
		return ResponseEntity.ok(ApiResponse.success("Logs fetched successfully", logService.getAll()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<LogResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success("Log fetched successfully", logService.getById(id)));
	}

	@GetMapping("/by-service")
	public ResponseEntity<ApiResponse<List<LogResponse>>> getBySourceService(@RequestParam String serviceName) {
		return ResponseEntity.ok(ApiResponse.success(
				"Logs fetched successfully",
				logService.getBySourceService(serviceName)
		));
	}

	@GetMapping("/by-trace")
	public ResponseEntity<ApiResponse<List<LogResponse>>> getByTraceId(@RequestParam String traceId) {
		return ResponseEntity.ok(ApiResponse.success(
				"Logs fetched successfully",
				logService.getByTraceId(traceId)
		));
	}

	@GetMapping("/by-hash")
	public ResponseEntity<ApiResponse<List<LogResponse>>> getByHashId(@RequestParam String hashId) {
		return ResponseEntity.ok(ApiResponse.success(
				"Logs fetched successfully",
				logService.getByHashId(hashId)
		));
	}

}
