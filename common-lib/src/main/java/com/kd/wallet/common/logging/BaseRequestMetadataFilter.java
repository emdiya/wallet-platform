package com.kd.wallet.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class BaseRequestMetadataFilter extends OncePerRequestFilter {

    private static final int DEFAULT_BODY_CACHE_LIMIT = 1024 * 1024;

    protected void afterRequest(HttpServletRequest request,
                                HttpServletResponse response,
                                String traceId,
                                String hashId,
                                long durationMs,
                                String requestBody,
                                String responseBody) {
        // Default no-op. Services can override to add request-level logging.
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = request instanceof ContentCachingRequestWrapper cachingRequest
                ? cachingRequest
                : new ContentCachingRequestWrapper(request, DEFAULT_BODY_CACHE_LIMIT);
        ContentCachingResponseWrapper responseWrapper = response instanceof ContentCachingResponseWrapper cachingResponse
                ? cachingResponse
                : new ContentCachingResponseWrapper(response);

        long startedAt = System.currentTimeMillis();
        String traceId = RequestMetadataUtils.traceIdOrGenerate(requestWrapper.getHeader(RequestMetadataUtils.TRACE_ID_HEADER));
        String requestId = RequestMetadataUtils.requestIdOrGenerate(requestWrapper.getHeader(RequestMetadataUtils.REQUEST_ID_HEADER));
        String hashId = requestWrapper.getHeader(RequestMetadataUtils.HASH_ID_HEADER);
        if (hashId == null || hashId.isBlank()) {
            hashId = RequestMetadataUtils.createHashId(requestWrapper.getMethod(), requestWrapper.getRequestURI(), traceId);
        }

        RequestMetadataContext.set(traceId, hashId, requestId);
        responseWrapper.setHeader(RequestMetadataUtils.TRACE_ID_HEADER, traceId);
        responseWrapper.setHeader(RequestMetadataUtils.HASH_ID_HEADER, hashId);
        responseWrapper.setHeader(RequestMetadataUtils.REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            afterRequest(
                    requestWrapper,
                    responseWrapper,
                    traceId,
                    hashId,
                    System.currentTimeMillis() - startedAt,
                    readBody(requestWrapper.getContentAsByteArray(), requestWrapper.getCharacterEncoding()),
                    readBody(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding())
            );
            responseWrapper.copyBodyToResponse();
            RequestMetadataContext.clear();
        }
    }

    private String readBody(byte[] body, String characterEncoding) {
        if (body == null || body.length == 0) {
            return "";
        }
        Charset charset = characterEncoding == null || characterEncoding.isBlank()
                ? StandardCharsets.UTF_8
                : Charset.forName(characterEncoding);
        return new String(body, charset);
    }
}
