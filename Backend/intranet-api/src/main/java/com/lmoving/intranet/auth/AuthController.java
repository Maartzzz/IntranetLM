package com.lmoving.intranet.auth;

import com.lmoving.intranet.auth.dto.CurrentUserResponse;
import com.lmoving.intranet.auth.dto.LoginRequest;
import com.lmoving.intranet.auth.dto.LoginResponse;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;
import com.lmoving.intranet.authorization.service.PermissionService;
import com.lmoving.intranet.audit.AuditLogService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final UserRepository userRepository;
	private final PermissionService permissionService;
	private final AuditLogService auditLogService;

	public AuthController(AuthService authService, UserRepository userRepository,
			PermissionService permissionService, AuditLogService auditLogService) {
		this.authService = authService;
		this.userRepository = userRepository;
		this.permissionService = permissionService;
		this.auditLogService = auditLogService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(
			@Valid @RequestBody LoginRequest request
	) {
		LoginResponse response;
		try {
			response = authService.login(request);
		} catch (AuthenticationException e) {
			auditLogService.registrarAutenticacion(request.getUsername(), false);
			return ResponseEntity.status(401).build();
		}

		auditLogService.registrarAutenticacion(request.getUsername(), true);
		return ResponseEntity.ok(response);

	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
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
						user.getRole().name(),
					permissionService.getUserPermissions(user.getUsername())
				)
		);
	}
}