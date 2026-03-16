package com.demo.common.exception;

public interface ErrorCode {
	int SUCCESS = 200;
	int BAD_REQUEST = 400;
	int UNAUTHORIZED = 401;
	int FORBIDDEN = 403;
	int NOT_FOUND = 404;
	int INTERNAL_ERROR = 500;

	// User errors
	int USER_NOT_FOUND = 1001;
	int USER_ALREADY_EXISTS = 1002;
	int INVALID_PASSWORD = 1003;

	// Product errors
	int PRODUCT_NOT_FOUND = 2001;
	int PRODUCT_STOCK_INSUFFICIENT = 2002;

	// Order errors
	int ORDER_NOT_FOUND = 3001;
	int ORDER_STATUS_INVALID = 3002;
}
