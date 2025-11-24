package com.example.upvote;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int upvotes = 0;   // must exist for increment/remove

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------
    public Post() {}

    public Post(String title) {
        this.title = title;
    }

    // -------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    // -------------------------------------------------------
    // Increment helper (used by POST /upvote)
    // -------------------------------------------------------
    public void increment() {
        this.upvotes++;
    }
}
