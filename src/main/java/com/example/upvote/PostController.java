package com.example.upvote;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostRepository repo;

    public PostController(PostRepository repo) {
        this.repo = repo;
    }

    // -------------------------------------------------------
    // List all posts
    // -------------------------------------------------------
    @GetMapping
    public List<Post> all() {
        return repo.findAll();
    }

    // -------------------------------------------------------
    // Get a single post
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Post> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -------------------------------------------------------
    // Create a post (basic demo)
    // -------------------------------------------------------
    @PostMapping
    public Post create(@RequestBody Post p) {
        return repo.save(p);
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
        return repo.findById(id).map(post -> {
            // ensure post has increment logic (Post.increment())
            post.increment();
            repo.save(post);

            // return the post (or you can return a JSON with count)
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

        return repo.findById(id).map(post -> {

            int current = post.getUpvotes();
            if (current > 0) {
                post.setUpvotes(current - 1);
            }

            repo.save(post);

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
        return repo.findById(id).map(post -> {
            repo.delete(post);
            Map<String,Object> resp = new HashMap<>();
            resp.put("deleted", true);
            resp.put("id", id);
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
