package com.demo.order.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.demo.common.exception.BusinessException;
import com.demo.common.response.Result;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public Result<Void> handleBusinessException(BusinessException e) {
		log.warn("Business exception [code={}]: {}", e.getCode(), e.getMessage());
		return Result.error(e.getCode(), e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("; "));
		return Result.error(400, message);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Result<Void> handleException(Exception e) {
		log.error("Unexpected error", e);
		return Result.error(500, "服务器内部错误");
	}
}
