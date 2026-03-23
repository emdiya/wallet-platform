package com.kd.wallet.wallet.config;

import com.kd.wallet.common.logging.RequestMetadataUtils;
import com.kd.wallet.wallet.logging.RequestLoggingFilter;
import com.kd.wallet.wallet.security.InternalApiKeyFilter;
import com.kd.wallet.wallet.security.JwtAuthenticationEntryPoint;
import com.kd.wallet.wallet.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final InternalApiKeyFilter internalApiKeyFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final RequestLoggingFilter requestLoggingFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
			InternalApiKeyFilter internalApiKeyFilter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
			RequestLoggingFilter requestLoggingFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.internalApiKeyFilter = internalApiKeyFilter;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.requestLoggingFilter = requestLoggingFilter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/api/wallets/me", "/api/wallets/me/**").authenticated()
				.requestMatchers("/error").permitAll()
				.anyRequest().permitAll())
				.formLogin(form -> form.disable())
				.addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(internalApiKeyFilter, JwtAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of("*"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of(
				"Authorization",
				RequestMetadataUtils.TRACE_ID_HEADER,
				RequestMetadataUtils.HASH_ID_HEADER,
				RequestMetadataUtils.REQUEST_ID_HEADER));
		configuration.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
