package com.demo.product.lock;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.demo.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 分布式锁 AOP 切面。
 *
 * <p>
 * 拦截所有 {@link DistributedLock} 注解的方法：
 * <ol>
 * <li>解析 SpEL key 表达式，绑定方法参数为上下文变量</li>
 * <li>自旋等待获取 Redis 锁（间隔 50ms），超过 waitTime 则抛出异常</li>
 * <li>执行目标方法</li>
 * <li>finally 块中释放锁，保证锁必然被释放</li>
 * </ol>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

	private static final ExpressionParser SPEL = new SpelExpressionParser();

	private final DistributedLockService lockService;

	@Around("@annotation(distributedLock)")
	public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
		String lockKey = resolveSpelKey(joinPoint, distributedLock.key());
		long deadline = System.currentTimeMillis() + distributedLock.waitTime();

		String token = null;
		while (System.currentTimeMillis() < deadline) {
			token = lockService.tryLock(lockKey, distributedLock.leaseTime());
			if (token != null) {
				break;
			}
			Thread.sleep(50); // 短暂退避后重试（虚拟线程友好）
		}

		if (token == null) {
			log.warn("Failed to acquire distributed lock, key={}", lockKey);
			throw new BusinessException(5001, "系统繁忙，请稍后重试");
		}

		log.debug("Acquired distributed lock: key={}", lockKey);
		try {
			return joinPoint.proceed();
		} finally {
			lockService.releaseLock(lockKey, token);
			log.debug("Released distributed lock: key={}", lockKey);
		}
	}

	/**
	 * 解析 SpEL 表达式，将方法参数绑定为 #paramName 变量。
	 * 需要编译时开启 -parameters，Spring Boot 插件默认开启。
	 */
	private String resolveSpelKey(ProceedingJoinPoint joinPoint, String spel) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		Parameter[] parameters = method.getParameters();
		Object[] args = joinPoint.getArgs();

		EvaluationContext context = new StandardEvaluationContext();
		for (int i = 0; i < parameters.length; i++) {
			context.setVariable(parameters[i].getName(), args[i]);
		}

		Expression expression = SPEL.parseExpression(spel);
		return expression.getValue(context, String.class);
	}
}
