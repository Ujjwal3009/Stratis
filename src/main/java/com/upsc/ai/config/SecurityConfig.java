package com.upsc.ai.config;

import com.upsc.ai.security.JwtAuthenticationFilter;
import com.upsc.ai.security.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Autowired
        private OAuth2SuccessHandler oAuth2SuccessHandler;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/api/v1/auth/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/api/auth/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/oauth2/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/login/oauth2/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/actuator/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/h2-console/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/swagger-ui/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/v3/api-docs/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/swagger-ui.html"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/api/v1/health/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/api/v1/subjects/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/api/health/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/api/subjects/**"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/error"))
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                                                                .antMatcher("/api/v1/admin/**"))
                                                .hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(oAuth2SuccessHandler))
                                .exceptionHandling(exceptions -> exceptions
                                                .defaultAuthenticationEntryPointFor(
                                                                new org.springframework.security.web.authentication.HttpStatusEntryPoint(
                                                                                org.springframework.http.HttpStatus.UNAUTHORIZED),
                                                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                                                                                "/api/**")))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                // Allow H2 console frames
                http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}
