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
    // repository may be unavailable on EC2 (we'll fallback to an in-memory store)
    private AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    // in-memory fallback store (used only when users == null)
    private final java.util.concurrent.ConcurrentMap<String, AppUser> inMemoryUsers = new java.util.concurrent.ConcurrentHashMap<>();


    // allow AppUserRepository to be absent. PasswordEncoder is required.
    @org.springframework.beans.factory.annotation.Autowired
    public AppUserService(java.util.Optional<AppUserRepository> usersOpt, PasswordEncoder passwordEncoder) {
        this.users = usersOpt.orElse(null);
        this.passwordEncoder = passwordEncoder;
    }


    // --- Spring Security user lookup used during authentication ---
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser;
        if (users != null) {
            appUser = users.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } else {
            appUser = inMemoryUsers.get(username);
            if (appUser == null) throw new UsernameNotFoundException("User not found: " + username);
        }


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
        if (users != null) {
            if (users.findByUsername(user.getUsername()).isPresent()) {
                log.warn("Username '{}' already exists", user.getUsername());
                throw new IllegalArgumentException("Username already exists");
            }
        } else {
            if (inMemoryUsers.containsKey(user.getUsername())) {
                log.warn("Username '{}' already exists (in-memory)", user.getUsername());
                throw new IllegalArgumentException("Username already exists");
            }
        }

// ... password hashing and role setting remain the same ...

        AppUser saved;
        if (users != null) {
            saved = users.save(user);
        } else {
            // in-memory save (do not call setId — AppUser has no setId(String))
            inMemoryUsers.put(user.getUsername(), user);
            saved = user;
        }


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
        if (users != null) {
            Optional<AppUser> opt = users.findByUsername(username);
            return opt.orElse(null);
        } else {
            return inMemoryUsers.get(username);
        }

    }

    // --- OIDC helper: create or update a user coming from Google (or other provider) ---
    @Transactional
    public void createOrUpdateFromOidc(String email, OidcUser oidcUser) {
        AppUser user = null;
        if (users != null) {
            user = users.findByUsername(email)
                    .orElseGet(() -> {
                        AppUser newUser = new AppUser();
                        newUser.setUsername(email);
                        newUser.setRole("USER");
                        newUser.setPasswordHash("");
                        return users.save(newUser);
                    });
        } else {
            user = inMemoryUsers.computeIfAbsent(email, e -> {
                AppUser newUser = new AppUser();
                newUser.setUsername(e);
                newUser.setRole("USER");
                newUser.setPasswordHash("");
                // no id setter available — leave id as-is
                return newUser;

            });
        }
        log.debug("OIDC user processed: {}", email);

    }
}
