package com.demo.product.lock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁注解（Redis SET NX PX 实现）。
 *
 * <p>
 * 使用示例：
 * 
 * <pre>{@code
 * &#64;DistributedLock(key = "'stock:' + #id", leaseTime = 5000, waitTime = 3000)
 * public void deductStock(Long id, Integer quantity) { ... }
 * }</pre>
 *
 * <p>
 * {@link #key()} 支持 SpEL 表达式，方法参数通过 #{参数名} 引用。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

	/**
	 * SpEL 表达式，计算结果作为 Redis 锁的 key 后缀。
	 * 例如：{@code "'stock:' + #id"}
	 */
	String key();

	/**
	 * 锁的最大持有时间（毫秒），防止持锁方宕机后死锁。默认 5 秒。
	 */
	long leaseTime() default 5000;

	/**
	 * 获取锁的最大等待时间（毫秒），超时则抛出异常。默认 3 秒。
	 */
	long waitTime() default 3000;
}
