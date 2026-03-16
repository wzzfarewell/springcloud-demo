package com.demo.product.lock;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 分布式锁服务（SET NX PX 实现）。
 *
 * <p>
 * 核心原语：
 * 
 * <pre>
 *   SET lockKey uniqueToken PX leaseTimeMillis NX
 * </pre>
 * 
 * NX = 仅当 key 不存在时才设置（原子操作，无竞争）。
 * PX = 毫秒级过期时间（防止持锁方宕机后死锁）。
 *
 * <p>
 * 释放锁时必须校验 token，确保只有锁持有者才能释放，
 * 避免误删其他线程/节点的锁（注意：check-then-delete 非原子，
 * 生产环境可改用 Lua 脚本实现原子释放）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockService {

	private static final String LOCK_PREFIX = "lock:";

	private final StringRedisTemplate stringRedisTemplate;

	/**
	 * 尝试获取锁。
	 *
	 * @param lockKey         锁名（不带前缀）
	 * @param leaseTimeMillis 锁自动过期时间（毫秒）
	 * @return 成功返回唯一 token（用于释放），失败返回 null
	 */
	public String tryLock(String lockKey, long leaseTimeMillis) {
		String fullKey = LOCK_PREFIX + lockKey;
		String token = UUID.randomUUID().toString();
		Boolean acquired = stringRedisTemplate.opsForValue()
				.setIfAbsent(fullKey, token, leaseTimeMillis, TimeUnit.MILLISECONDS);
		return Boolean.TRUE.equals(acquired) ? token : null;
	}

	/**
	 * 释放锁（校验 token 归属，防止误释放）。
	 *
	 * @param lockKey 锁名（不带前缀）
	 * @param token   获取锁时返回的唯一标识
	 */
	public void releaseLock(String lockKey, String token) {
		String fullKey = LOCK_PREFIX + lockKey;
		String current = stringRedisTemplate.opsForValue().get(fullKey);
		if (token.equals(current)) {
			stringRedisTemplate.delete(fullKey);
		} else {
			// 锁已过期被其他线程获取，不做操作（避免释放他人的锁）
			log.warn("Lock token mismatch or expired, key={}", fullKey);
		}
	}
}
