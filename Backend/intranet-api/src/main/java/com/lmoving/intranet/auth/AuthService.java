package com.lmoving.intranet.auth;

import com.lmoving.intranet.auth.dto.LoginRequest;
import com.lmoving.intranet.auth.dto.LoginResponse;
import com.lmoving.intranet.security.JwtService;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final JwtService jwtService;

	public AuthService(
		AuthenticationManager authenticationManager,
		UserRepository userRepository,
		JwtService jwtService
	) {
	this.authenticationManager = authenticationManager;
	this.userRepository = userRepository;
	this.jwtService = jwtService;
	}

	public LoginResponse login(LoginRequest request) {

	authenticationManager.authenticate(
		new UsernamePasswordAuthenticationToken(
			request.getUsername(),
			request.getPassword()
		)
	);

	User user = userRepository
		.findByUsername(request.getUsername())
		.orElseThrow();

	String token = jwtService.generateToken(
		user.getUsername(),
		user.getRole().name()
	);

	return new LoginResponse(
		token,
		"Bearer"
	);
	}
}