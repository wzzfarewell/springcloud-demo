package com.demo.user.service.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.common.exception.BusinessException;
import com.demo.user.dto.LoginRequest;
import com.demo.user.dto.RegisterRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.demo.common.constant.RabbitMQConstants;
import com.demo.common.constant.RedisKeyConstants;
import com.demo.common.dto.UserDTO;
import com.demo.user.entity.User;
import com.demo.user.repository.UserRepository;
import com.demo.user.service.JwtService;
import com.demo.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RedisTemplate<String, Object> redisTemplate;
	private final RabbitTemplate rabbitTemplate;

	@Override
	@Transactional
	public UserDTO register(RegisterRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new BusinessException(1002, "用户名已存在");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new BusinessException(1002, "邮箱已被注册");
		}

		User user = User.builder()
				.username(request.getUsername())
				.password(passwordEncoder.encode(request.getPassword()))
				.email(request.getEmail())
				.phone(request.getPhone())
				.role("USER")
				.build();

		user = userRepository.save(user);
		UserDTO userDTO = toDTO(user);

		// 发布用户注册事件到 RabbitMQ
		try {
			rabbitTemplate.convertAndSend(
					RabbitMQConstants.USER_EXCHANGE,
					RabbitMQConstants.USER_CREATED_ROUTING_KEY,
					userDTO);
			log.info("Published user.created event for user: {}", user.getUsername());
		} catch (Exception e) {
			log.warn("Failed to publish user.created event: {}", e.getMessage());
		}

		return userDTO;
	}

	@Override
	public String login(LoginRequest request) {
		User user = userRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new BusinessException(1001, "用户不存在"));
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new BusinessException(1003, "密码错误");
		}
		if (!user.getEnabled()) {
			throw new BusinessException(403, "账号已被禁用");
		}
		return jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
	}

	@Override
	public UserDTO getUserById(Long id) {
		String cacheKey = RedisKeyConstants.USER_KEY_PREFIX + id;
		UserDTO cached = (UserDTO) redisTemplate.opsForValue().get(cacheKey);
		if (cached != null) {
			log.debug("Cache hit for user id: {}", id);
			return cached;
		}
		User user = userRepository.findById(id)
				.orElseThrow(() -> new BusinessException(1001, "用户不存在"));
		UserDTO userDTO = toDTO(user);
		redisTemplate.opsForValue().set(cacheKey, userDTO, RedisKeyConstants.USER_CACHE_TTL, TimeUnit.SECONDS);
		return userDTO;
	}

	@Override
	public UserDTO getUserByUsername(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new BusinessException(1001, "用户不存在"));
		return toDTO(user);
	}

	@Override
	public List<UserDTO> getAllUsers() {
		return userRepository.findAll().stream()
				.map(this::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public UserDTO updateUser(Long id, UserDTO userDTO) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new BusinessException(1001, "用户不存在"));
		if (userDTO.getEmail() != null)
			user.setEmail(userDTO.getEmail());
		if (userDTO.getPhone() != null)
			user.setPhone(userDTO.getPhone());
		user = userRepository.save(user);
		// 使 Redis 缓存失效
		redisTemplate.delete(RedisKeyConstants.USER_KEY_PREFIX + id);
		return toDTO(user);
	}

	@Override
	@Transactional
	public void deleteUser(Long id) {
		if (!userRepository.existsById(id)) {
			throw new BusinessException(1001, "用户不存在");
		}
		userRepository.deleteById(id);
		redisTemplate.delete(RedisKeyConstants.USER_KEY_PREFIX + id);
	}

	private UserDTO toDTO(User user) {
		return UserDTO.builder()
				.id(user.getId())
				.username(user.getUsername())
				.email(user.getEmail())
				.phone(user.getPhone())
				.role(user.getRole())
				.createdAt(user.getCreatedAt())
				.build();
	}
}
