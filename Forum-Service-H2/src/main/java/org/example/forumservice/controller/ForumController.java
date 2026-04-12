package org.example.forumservice.controller;

import org.example.forumservice.entity.ForumPost;
import org.example.forumservice.entity.Reply;
import org.example.forumservice.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class ForumController {

    @Autowired
    private ForumService forumService;

    // Public — anyone can read
    @GetMapping
    public List<ForumPost> getAllPosts() {
        return forumService.getAllPosts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForumPost> getPostById(@PathVariable Long id) {
        return forumService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CLIENT creates a post
    @PostMapping
    @PreAuthorize("hasRole('client')")
    public ResponseEntity<ForumPost> createPost(@RequestBody ForumPost post) {
        try {
            ForumPost savedPost = forumService.createPost(post);
            return ResponseEntity.ok(savedPost);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // CLIENT updates their post
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('client')")
    public ResponseEntity<ForumPost> updatePost(@PathVariable Long id, @RequestBody ForumPost post) {
        try {
            return ResponseEntity.ok(forumService.updatePost(id, post));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ADMIN deletes any post
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        forumService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // FREELANCER adds a reply
    @PostMapping("/{id}/replies")
    @PreAuthorize("hasRole('freelancer')")
    public ResponseEntity<Reply> addReply(@PathVariable Long id, @RequestBody Reply reply) {
        try {
            return ResponseEntity.ok(forumService.addReply(id, reply));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Public — anyone can read replies
    @GetMapping("/{id}/replies")
    public List<Reply> getReplies(@PathVariable Long id) {
        return forumService.getRepliesByPost(id);
    }

    // ADMIN deletes any reply
    @DeleteMapping("/replies/{replyId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteReply(@PathVariable Long replyId) {
        forumService.deleteReply(replyId);
        return ResponseEntity.noContent().build();
    }

    // CLIENT or FREELANCER can react
    @PostMapping("/{id}/react")
    @PreAuthorize("hasRole('client') or hasRole('freelancer')")
    public ResponseEntity<ForumPost> addReaction(
            @PathVariable Long id,
            @RequestParam String emoji) {
        try {
            return ResponseEntity.ok(forumService.addReaction(id, emoji));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}