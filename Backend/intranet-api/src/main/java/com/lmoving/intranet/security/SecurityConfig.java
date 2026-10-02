package com.lmoving.intranet.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Value("${app.cors.allowed-origin:http://localhost:4200}")
    private String allowedOrigin;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors( cors -> {})

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .exceptionHandling(exception -> exception

            .authenticationEntryPoint(
                (request, response, authException) -> {

                    response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentication required"
                    );
                }
            )

                .accessDeniedHandler(
                    (request, response, accessDeniedException) -> {
                        response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "Access denied"
                        );
                    }
                )
            )
            
            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/api/auth/**", "/error").permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/drivers"
                ).hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/drivers"
                ).hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/drivers/*"
                ).hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/drivers/*"
                ).hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/courses/**"
                ).hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/courses/**"
                ).hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/courses/**"
                ).hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/questions/**"
                ).hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/questions/**"
                ).hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/questions/**"
                ).hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/questions/**"
                ).hasAnyRole("ADMIN", "SUPERVISOR")

                .anyRequest().authenticated()
            )


            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
    
        CorsConfiguration configuration = new CorsConfiguration();
    
        configuration.setAllowedOrigins(
            List.of(allowedOrigin)
        );
    
        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        );
    
        configuration.setAllowedHeaders(
            List.of("Authorization", "Content-Type")
        );
    
        configuration.setAllowCredentials(false);
    
        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
    
        source.registerCorsConfiguration("/**", configuration);
    
        return source;
    }
}