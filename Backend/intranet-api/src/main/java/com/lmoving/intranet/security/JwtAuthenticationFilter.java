package com.lmoving.intranet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;

	public JwtAuthenticationFilter(
			JwtService jwtService,
			CustomUserDetailsService userDetailsService
	) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		return ("POST".equalsIgnoreCase(request.getMethod()) && "/api/auth/login".equals(path))
				|| "OPTIONS".equalsIgnoreCase(request.getMethod())
				|| "/error".equals(path);
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			filterChain.doFilter(request, response);
			return;
		}

		final String token = authHeader.substring(7).trim();

		try {
			if (token.isEmpty()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
				return;
			}

			final String username = jwtService.extractUsername(token);
			if (username == null || username.isBlank()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
				return;
			}

			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			if (!jwtService.isTokenValid(token, userDetails.getUsername()) || !userDetails.isEnabled()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
				return;
			}

			UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(
							userDetails,
							null,
							userDetails.getAuthorities()
						);

			authentication.setDetails(
					new WebAuthenticationDetailsSource().buildDetails(request)
			);

			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {
			SecurityContextHolder.clearContext();
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
			return;
		}

		filterChain.doFilter(request, response);
	}

}