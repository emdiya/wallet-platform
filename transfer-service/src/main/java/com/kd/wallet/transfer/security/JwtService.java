package com.kd.wallet.transfer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

	@Value("${auth.jwt.secret}")
	private String jwtSecret;

	private SecretKey signingKey;

	@PostConstruct
	void init() {
		this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
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

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
