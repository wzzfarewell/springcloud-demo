package com.demo.user.service;

import java.util.List;

import com.demo.user.dto.LoginRequest;
import com.demo.user.dto.RegisterRequest;

import com.demo.common.dto.UserDTO;

public interface UserService {
	UserDTO register(RegisterRequest request);

	String login(LoginRequest request);

	UserDTO getUserById(Long id);

	UserDTO getUserByUsername(String username);

	List<UserDTO> getAllUsers();

	UserDTO updateUser(Long id, UserDTO userDTO);

	void deleteUser(Long id);
}
