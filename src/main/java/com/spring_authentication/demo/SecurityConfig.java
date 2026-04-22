package com.spring_authentication.demo;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain anonymousSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/anonymous/**")
                .cors(cors -> cors.configurationSource(corsLimitedConfigurationSource()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(100)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatchers(cfg -> cfg.requestMatchers("/swagger-ui*", "/swagger-ui/**", "/v3/api-docs/**"));
        http.cors(cors -> cors.configurationSource(corsLimitedConfigurationSource()));
        http.authorizeHttpRequests(cfg -> cfg.anyRequest().permitAll());
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public SecurityFilterChain fallbackSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()).cors(cors -> cors.configurationSource(corsLimitedConfigurationSource()))
                .httpBasic(withDefaults());

        return http.build();
    }

    private CorsConfigurationSource corsLimitedConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:8080"));
            config.setAllowedMethods(List.of("GET", "POST"));
            config.setAllowedHeaders(List.of("*"));
            return config;
        };
    }

    @Bean
    public UserDetailsService sharedUserDetailsService(PasswordEncoder encoder) {
        User.UserBuilder ubuilder = User.builder().passwordEncoder(encoder::encode);

        List<UserDetails> users = List.of(ubuilder.username("user1").password("password1").roles().build(),
                ubuilder.username("user2").password("password2").roles().build());

        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
