package com.example.movieapp.controller;

import com.example.movieapp.model.MovieDTO;
import com.example.movieapp.model.Review;
import com.example.movieapp.repository.ReviewRepository;
import com.example.movieapp.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for handling review-related operations:
 * writing, saving, editing, viewing, and deleting reviews.
 */
@Controller
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MovieService movieService;

    /**
     * Display the review form with pre-filled movie data.
     */
    @GetMapping("/write")
    public String showReviewForm(@RequestParam String title,
                                 @RequestParam String posterPath,
                                 @RequestParam(required = false) String genre,
                                 @RequestParam("tmdbId") String tmdbId,
                                 @RequestParam(required = false) Integer duration,
                                 @RequestParam(required = false) String type,
                                 Model model) {

        System.out.println("=== showReviewForm called ===");

        Review review = new Review();
        review.setTitle(title);
        review.setTmdbId(tmdbId);
        review.setPosterPath(posterPath);
        review.setRating(5.0); // Default rating

        // Try to enrich movie details using TMDB ID and type
        if (tmdbId != null && type != null) {
            MovieDTO dto = new MovieDTO();
            try {
                dto.setId(Integer.parseInt(tmdbId));
                movieService.enrichMovieDetails(dto, type);
                review.setGenre(dto.getGenre());
                review.setDuration(dto.getDuration());
                review.setType(type);
            } catch (NumberFormatException e) {
                // Fallback if tmdbId is not a number
                review.setGenre(genre != null ? genre : "Unknown");
                review.setDuration(duration != null ? duration : 0);
            } catch (Exception e) {
                // Fallback if enrichment fails
                review.setGenre(genre != null ? genre : "Unknown");
                review.setDuration(duration != null ? duration : 0);
            }
        } else {
            // Use provided genre and duration if enrichment is not possible
            review.setGenre(genre != null ? genre : "Unknown");
            review.setDuration(duration != null ? duration : 0);
        }

        model.addAttribute("review", review);
        return "review"; // View for writing a review
    }

    /**
     * Display the logged-in user's reviews on their personal page.
     */
    @GetMapping("/reviews")
    public String showReviews(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        List<Review> reviews = reviewRepository.findByUsername(username);
        model.addAttribute("reviews", reviews);
        return "mypage/reviews";
    }

    /**
     * Save a new review submitted by the user.
     */
    @PostMapping("/save")
    public String saveReview(@ModelAttribute Review review,
                             @AuthenticationPrincipal UserDetails userDetails, Model model) {

        review.setUsername(userDetails.getUsername());
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);
        model.addAttribute("message", "Saved!");
        return "saved"; // Confirmation page
    }

    /**
     * Display the edit form for a specific review.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null || !review.getUsername().equals(userDetails.getUsername())) {
            return "redirect:/review/reviews";
        }
        model.addAttribute("review", review);
        return "edit-review";
    }

    /**
     * Update an existing review.
     */
    @PostMapping("/update")
    public String updateReview(@ModelAttribute Review review,
                               @AuthenticationPrincipal UserDetails userDetails) {
        Review existing = reviewRepository.findById(review.getId()).orElse(null);
        if (existing == null || !existing.getUsername().equals(userDetails.getUsername())) {
            return "redirect:/review/reviews";
        }

        existing.setContent(review.getContent());
        existing.setRating(review.getRating());
        existing.setCreatedAt(LocalDateTime.now());
        existing.setDuration(review.getDuration() != null ? review.getDuration() : 0);
        existing.setGenre(review.getGenre());
        existing.setType(review.getType());

        reviewRepository.save(existing);
        return "redirect:/review/reviews";
    }

    /**
     * Delete a review by ID.
     */
    @GetMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null || !review.getUsername().equals(userDetails.getUsername())) {
            return "redirect:/review/reviews";
        }
        reviewRepository.deleteById(id);
        return "redirect:/review/reviews";
    }
}