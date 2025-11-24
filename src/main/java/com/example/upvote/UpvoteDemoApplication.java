package com.example.upvote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UpvoteDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpvoteDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner createInitialUser(
            AppUserRepository repo,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder
    ) {
        return args -> {
            String username = "Jhansi";      // initial username
            String rawPassword = "Teji";    // initial password

            repo.findByUsername(username).ifPresentOrElse(
                    u -> {
                        // user already exists — do nothing
                    },
                    () -> {
                        String hashed = passwordEncoder.encode(rawPassword);
                        AppUser u = new AppUser(username, hashed, "USER");
                        repo.save(u);
                        System.out.println("Created initial user: " + username);
                    }
            );
        };
    }
}
