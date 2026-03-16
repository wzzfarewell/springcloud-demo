package com.demo.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

/**
 * JWT 全局鉴权过滤器，在网关层统一校验 Token，
 * 校验通过后将用户信息写入下游请求头，服务间调用无需重复验证。
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

	@Value("${jwt.secret}")
	private String jwtSecret;

	private static final List<String> WHITE_LIST = List.of(
			"/api/users/login",
			"/api/users/register",
			"/actuator");

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getPath().value();
		if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
			return chain.filter(exchange);
		}

		String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
			return unauthorized(exchange);
		}

		String token = authHeader.substring(7);
		try {
			SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
			Claims claims = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload();

			// 将用户信息透传到下游微服务
			ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
					.header("X-User-Id", claims.getSubject())
					.header("X-User-Name", claims.get("username", String.class))
					.header("X-User-Role", claims.get("role", String.class))
					.build();
			return chain.filter(exchange.mutate().request(mutatedRequest).build());
		} catch (Exception e) {
			return unauthorized(exchange);
		}
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		return response.setComplete();
	}

	@Override
	public int getOrder() {
		return -100;
	}
}
