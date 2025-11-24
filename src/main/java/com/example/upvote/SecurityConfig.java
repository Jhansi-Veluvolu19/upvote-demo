package com.example.upvote;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // ok for local dev; enable for production
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/", "/login", "/login.html", "/perform_login",
                                "/css/**", "/js/**", "/favicon.ico", "/error",
                                "/oauth2/**", "/login/oauth2/**", "/h2-console/**",
                                "/posts/**",
                                "/auth/**"                 // <-- ADDED: allow testing endpoints
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/perform_login")
                        .permitAll()
                        .defaultSuccessUrl("/index.html", true)
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login.html")
                        .defaultSuccessUrl("/index.html", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );

        // allow H2 console frames for development
        http.headers().frameOptions().disable();

        return http.build();
    }
}
