package com.example.upvote;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user") // avoid reserved words
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role = "USER";

    public AppUser() {}

    public AppUser(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Existing getter for the stored (hashed) password.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Alias expected by some security/login code: return the hashed password.
     */
    public String getPassword() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
