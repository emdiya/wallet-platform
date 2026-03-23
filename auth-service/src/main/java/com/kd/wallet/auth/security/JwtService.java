package com.kd.wallet.auth.security;

import com.kd.wallet.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

	@Value("${auth.jwt.secret}")
	private String jwtSecret;

	@Value("${auth.jwt.expiration-seconds}")
	private long expirationSeconds;

	private SecretKey signingKey;

	@PostConstruct
	void init() {
		this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(expirationSeconds);

		return Jwts.builder()
				.subject(String.valueOf(user.getId()))
				.claim("phone", user.getPhone())
				.claim("fullName", user.getFullName())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt))
				.signWith(signingKey)
				.compact();
	}

	public boolean isTokenValid(String token) {
		try {
			extractAllClaims(token);
			return true;
		} catch (RuntimeException exception) {
			return false;
		}
	}

	public Long extractUserId(String token) {
		return Long.valueOf(extractAllClaims(token).getSubject());
	}

	public Instant extractExpiration(String token) {
		return extractAllClaims(token).getExpiration().toInstant();
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

}
