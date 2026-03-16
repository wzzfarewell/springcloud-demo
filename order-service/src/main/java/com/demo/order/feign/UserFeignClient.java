package com.demo.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.demo.common.response.Result;

import com.demo.common.dto.UserDTO;

/**
 * 通过 OpenFeign + Eureka 服务发现调用 user-service
 */
@FeignClient(name = "user-service")
public interface UserFeignClient {

	@GetMapping("/api/users/{id}")
	Result<UserDTO> getUserById(@PathVariable("id") Long id);
}
