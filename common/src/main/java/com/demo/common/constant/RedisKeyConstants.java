package com.demo.common.constant;

public interface RedisKeyConstants {
	String USER_KEY_PREFIX = "user:";
	String PRODUCT_KEY_PREFIX = "product:";
	String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

	long USER_CACHE_TTL = 30 * 60L; // 30 minutes
	long PRODUCT_CACHE_TTL = 60 * 60L; // 1 hour
}
