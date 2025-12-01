package com.example.upvote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@SpringBootApplication(
        scanBasePackages = "com.example.upvote",
        exclude = {
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
        }
)

public class UpvoteDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpvoteDemoApplication.class, args);
    }

    // Only run initial-user creation when a repository is actually present.
    @Bean
    CommandLineRunner createInitialUser(
            Optional<AppUserRepository> repoOpt,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (repoOpt.isEmpty()) {
                // No DB / repository available — skip initial user creation.
                return;
            }

            AppUserRepository repo = repoOpt.get();

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
