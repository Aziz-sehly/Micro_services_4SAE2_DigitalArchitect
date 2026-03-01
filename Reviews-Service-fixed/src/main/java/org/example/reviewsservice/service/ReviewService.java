package org.example.reviewsservice.service;

import org.example.reviewsservice.entity.Review;
import org.example.reviewsservice.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    // Basic profanity word list - extend as needed
    private static final List<String> PROFANITY_LIST = Arrays.asList(
            "badword1", "badword2", "damn", "crap", "idiot", "stupid"
    );

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public Review createReview(Review review) {
        // Apply profanity filter
        review.setContent(censorProfanity(review.getContent()));

        // Calculate and update average rating
        Review saved = reviewRepository.save(review);
        Double avg = reviewRepository.findAverageRating();
        saved.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : review.getRating());
        return reviewRepository.save(saved);
    }

    public Review updateReview(Long id, Review updated) {
        return reviewRepository.findById(id).map(review -> {
            review.setAuthor(updated.getAuthor());
            review.setContent(censorProfanity(updated.getContent()));
            review.setRating(updated.getRating());
            review.setLanguage(updated.getLanguage());
            Double avg = reviewRepository.findAverageRating();
            review.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : updated.getRating());
            return reviewRepository.save(review);
        }).orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    public Double getAverageRating() {
        Double avg = reviewRepository.findAverageRating();
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    private String censorProfanity(String text) {
        if (text == null) return null;
        String result = text;
        for (String word : PROFANITY_LIST) {
            String censored = "*".repeat(word.length());
            result = result.replaceAll("(?i)" + word, censored);
        }
        return result;
    }
}