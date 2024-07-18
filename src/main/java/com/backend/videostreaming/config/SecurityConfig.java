package com.backend.videostreaming.config;

import com.backend.videostreaming.security.JwtAuthenticationEntryPoint;
import com.backend.videostreaming.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {


    @Autowired
    private JwtAuthenticationEntryPoint point;
    @Autowired
    private JwtAuthenticationFilter filter;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // configures the security filter chain, which is a series of filters that will be applied to incoming HTTP requests.
    // method is called automatically by Spring Security during the startup of the Spring Boot application.
    // This method is responsible for configuring the security settings for the application, including the JWT authentication setup.
    // When the Spring Boot application starts, Spring Security looks for beans of type SecurityFilterChain to configure the security filter chain.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/home/**").authenticated() // Configuring Authorization Rules:
                        .requestMatchers("/auth/login", "/auth/signup", "/auth/send-otp", "/auth/reset-password", "/auth/reset-password-token").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(point)) // This will respond with a 401 Unauthorized status and a custom message if the user is not authenticated.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Adds the custom JwtAuthenticationFilter before the UsernamePasswordAuthenticationFilter in the filter chain.
        // This ensures that JWT authentication is processed before the standard username-password authentication.
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // Add your frontend URL
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // a specific type of AuthenticationProvider that uses a UserDetailsService to retrieve user details and a PasswordEncoder to validate passwords.
    // automatically called by Spring when it initializes the application context. The significance of this bean lies in how it configures Spring Security to authenticate users.
    // Login Attempt --> authentication manager delegates process to DaoAuthenticationProvider --> DaoAuthenticationProvider uses the configured UserDetailsService to load the user by their username and PasswordEncoder to compare the submitted password with the stored password.
    @Bean
    public DaoAuthenticationProvider doDaoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

}
