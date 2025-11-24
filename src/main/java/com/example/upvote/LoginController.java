package com.example.upvote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.example.upvote.AppUserService;   // <-- added import

/**
 * Minimal controller: only POST JSON endpoint retained.
 */
@RestController
@RequestMapping("/auth")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    // STEP 1: Inject the AppUserService
    private final AppUserService appUserService;

    public LoginController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    // POST /auth/register  Content-Type: application/json
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> register(@RequestBody RegisterRequest req) {

        // For now: only print the username.
        log.info("register received for username='{}'", req.getUsername());

        // Convert RegisterRequest → AppUser
        AppUser user = new AppUser();
        user.setUsername(req.getUsername());
        user.setPasswordHash(req.getPassword()); // temporarily storing raw password

        appUserService.registerNewUser(user);

        // We will save to MySQL in Step 2.
        return ResponseEntity.status(201).body("User received");
    }

    // Simple DTO used only for JSON mapping in this controller
    public static class RegisterRequest {
        private String firstName;
        private String lastName;
        private String username;
        private String password;

        public RegisterRequest() {}

        public RegisterRequest(String firstName, String lastName, String username, String password) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.username = username;
            this.password = password;
        }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
