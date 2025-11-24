package com.example.upvote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

/**
 * AppUserService:
 * - provides Spring Security user lookup (loadUserByUsername)
 * - provides register/registerNewUser to save new users (with BCrypt password hashing)
 * - provides a helper for OIDC users (createOrUpdateFromOidc)
 */
@Service
public class AppUserService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(AppUserService.class);
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Spring Security user lookup used during authentication ---
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + appUser.getRole());
        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .authorities(List.of(authority))
                .build();
    }

    // --- Register a new AppUser and return the saved entity ---
    @Transactional
    public AppUser register(AppUser user) {
        log.info("Registering new user '{}'", user.getUsername());

        // Basic uniqueness check
        if (users.findByUsername(user.getUsername()).isPresent()) {
            log.warn("Username '{}' already exists", user.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        // Read plain password (AppUser#getPassword() returns the stored/hash or plain depending on flow)
        String raw = user.getPassword();
        if (raw == null) raw = "";

        // Hash the plain password -> store in passwordHash field
        String hash = passwordEncoder.encode(raw);
        user.setPasswordHash(hash);

        // set default role if not provided
        if (user.getRole() == null) user.setRole("USER");

        AppUser saved = users.save(user);
        log.info("Registered user id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    /**
     * Backwards-compatible method name used by controllers that call registerNewUser(...)
     */
    @Transactional
    public AppUser registerNewUser(AppUser user) {
        return register(user);
    }

    /**
     * Convenience finder that returns null when user not found (useful in some controllers).
     * If you prefer Optional, change the callers or expose an Optional-returning method.
     */
    public AppUser findByUsername(String username) {
        Optional<AppUser> opt = users.findByUsername(username);
        return opt.orElse(null);
    }

    // --- OIDC helper: create or update a user coming from Google (or other provider) ---
    @Transactional
    public void createOrUpdateFromOidc(String email, OidcUser oidcUser) {
        AppUser user = users.findByUsername(email)
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setUsername(email);
                    newUser.setRole("USER");
                    newUser.setPasswordHash(""); // no local password
                    return users.save(newUser);
                });

        // Optionally update profile fields from oidcUser.getAttributes() here
        log.debug("OIDC user processed: {}", email);
    }
}
