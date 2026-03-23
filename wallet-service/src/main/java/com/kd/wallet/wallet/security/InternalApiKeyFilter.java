package com.kd.wallet.wallet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

	public static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

	private final String expectedApiKey;

	public InternalApiKeyFilter(@Value("${internal.api-key}") String expectedApiKey) {
		this.expectedApiKey = expectedApiKey;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		if (!requiresInternalProtection(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		if (isCustomerWalletCreate(request)) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.isAuthenticated()) {
				filterChain.doFilter(request, response);
				return;
			}
		}

		String providedApiKey = request.getHeader(INTERNAL_API_KEY_HEADER);
		if (providedApiKey == null || !providedApiKey.equals(expectedApiKey)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("application/json");
			response.getWriter().write("{\"success\":false,\"message\":\"Forbidden\",\"data\":null}");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean requiresInternalProtection(HttpServletRequest request) {
		String path = request.getRequestURI();
		String method = request.getMethod();
		return isCustomerWalletCreate(request)
				|| HttpMethod.POST.matches(method) && (
				"/api/wallets/top-ups".equals(path)
						|| "/api/wallets/holds/reserve".equals(path)
						|| "/api/wallets/holds/commit".equals(path)
						|| "/api/wallets/holds/release".equals(path));
	}

	private boolean isCustomerWalletCreate(HttpServletRequest request) {
		return HttpMethod.POST.matches(request.getMethod()) && "/api/wallets".equals(request.getRequestURI());
	}
}
