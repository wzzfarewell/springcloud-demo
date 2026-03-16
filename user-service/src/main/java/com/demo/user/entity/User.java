package com.demo.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(unique = true, nullable = false, length = 50)
	private String username;

	@NotBlank
	@Column(nullable = false)
	private String password;

	@Email
	@Column(unique = true, nullable = false, length = 100)
	private String email;

	@Column(length = 20)
	private String phone;

	@Column(nullable = false, length = 20)
	@Builder.Default
	private String role = "USER";

	@Column(nullable = false)
	@Builder.Default
	private Boolean enabled = true;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;
}
