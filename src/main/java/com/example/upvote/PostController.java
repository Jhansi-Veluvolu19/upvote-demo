package com.example.upvote;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final Optional<PostRepository> repoOpt;

    public PostController(Optional<PostRepository> repoOpt) {
        this.repoOpt = repoOpt;
    }

    // -------------------------------------------------------
    // List all posts
    // -------------------------------------------------------
    @GetMapping
    public List<Post> all() {
        return repoOpt.map(PostRepository::findAll).orElse(Collections.emptyList());
    }

    // -------------------------------------------------------
    // Get a single post
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Post> get(@PathVariable Long id) {
        if (repoOpt.isEmpty()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        return repoOpt.get().findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -------------------------------------------------------
    // Create a post (basic demo)
    // -------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Post p) {
        if (repoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error","No repository available"));
        }
        Post saved = repoOpt.get().save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // -------------------------------------------------------
    // UPVOTE a post (increment)
    // -------------------------------------------------------
    @PostMapping("/{id}/upvote")
    @Transactional
    public ResponseEntity<?> upvote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user
    ) {
        if (repoOpt.isEmpty()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        return repoOpt.get().findById(id).map(post -> {
            post.increment();
            repoOpt.get().save(post);

            Map<String,Object> resp = new HashMap<>();
            resp.put("count", post.getUpvotes());
            resp.put("upvoted", true);
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.notFound().build());
    }

    // -------------------------------------------------------
    // REMOVE UPVOTE (decrement)
    // -------------------------------------------------------
    @DeleteMapping("/{id}/upvote")
    @Transactional
    public ResponseEntity<Map<String, Object>> removeUpvote(@PathVariable Long id) {
        if (repoOpt.isEmpty()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        return repoOpt.get().findById(id).map(post -> {

            int current = post.getUpvotes();
            if (current > 0) {
                post.setUpvotes(current - 1);
            }

            repoOpt.get().save(post);

            Map<String, Object> resp = new HashMap<>();
            resp.put("count", post.getUpvotes());
            resp.put("upvoted", false);

            return ResponseEntity.ok(resp);

        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // -------------------------------------------------------
    // DELETE a post (remove post by id)
    // -------------------------------------------------------
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String,Object>> deletePost(@PathVariable Long id) {
        if (repoOpt.isEmpty()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        return repoOpt.get().findById(id).map(post -> {
            repoOpt.get().delete(post);
            Map<String,Object> resp = new HashMap<>();
            resp.put("deleted", true);
            resp.put("id", id);
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
