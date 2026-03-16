package com.demo.user.controller;

import com.demo.common.dto.UserDTO;
import com.demo.common.response.Result;
import com.demo.user.dto.LoginRequest;
import com.demo.user.dto.RegisterRequest;
import com.demo.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public Result<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
		return Result.success(userService.register(request));
	}

	@PostMapping("/login")
	public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
		String token = userService.login(request);
		return Result.success(Map.of("token", token, "type", "Bearer"));
	}

	@GetMapping("/{id}")
	public Result<UserDTO> getUserById(@PathVariable Long id) {
		return Result.success(userService.getUserById(id));
	}

	@GetMapping("/username/{username}")
	public Result<UserDTO> getUserByUsername(@PathVariable String username) {
		return Result.success(userService.getUserByUsername(username));
	}

	@GetMapping
	public Result<List<UserDTO>> getAllUsers() {
		return Result.success(userService.getAllUsers());
	}

	@PutMapping("/{id}")
	public Result<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
		return Result.success(userService.updateUser(id, userDTO));
	}

	@DeleteMapping("/{id}")
	public Result<Void> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return Result.success();
	}
}
