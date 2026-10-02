package com.lmoving.intranet.auth;

import com.lmoving.intranet.auth.dto.CurrentUserResponse;
import com.lmoving.intranet.auth.dto.LoginRequest;
import com.lmoving.intranet.auth.dto.LoginResponse;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final UserRepository userRepository;

	public AuthController(AuthService authService, UserRepository userRepository) {
		this.authService = authService;
		this.userRepository = userRepository;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(
			@Valid @RequestBody LoginRequest request
	) {
		try {
			return ResponseEntity.ok(
					authService.login(request)
			);
		} catch (BadCredentialsException e) {
				return ResponseEntity
					.status(401)
					.build();
		}

	}

	@GetMapping("/me")
	public ResponseEntity<CurrentUserResponse> me(
			Authentication authentication
	) {

		User user = userRepository
				.findByUsername(authentication.getName())
				.orElseThrow();

		return ResponseEntity.ok(
				new CurrentUserResponse(
						user.getUsername(),
						user.getFirstName(),
						user.getLastName(),
						user.getRole().name()
				)
		);
	}
}