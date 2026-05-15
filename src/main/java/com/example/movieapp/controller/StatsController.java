package com.example.movieapp.controller;

import com.example.movieapp.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for displaying user statistics on their personal page.
 */
@Controller
@RequestMapping("/mypage")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * Displays various statistics for the logged-in user.
     */
    @GetMapping("/stats")
    public String showStats(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();

        // Basic stats
        model.addAttribute("totalWatchTime", statsService.getTotalWatchTime(username));
        model.addAttribute("averageRating", statsService.getAverageRating(username));
        model.addAttribute("favoriteGenre", statsService.getFavoriteGenre(username));
        model.addAttribute("totalReviews", statsService.getTotalReviews(username));

        // Number of items watched per genre
        Map<String, Long> genreCounts = statsService.getGenreCounts(username);
        model.addAttribute("genreLabels", genreCounts.keySet());
        model.addAttribute("genreData", genreCounts.values());

        // Average rating per genre
        Map<String, Double> genreAvgRatings = statsService.getGenreAverageRatings(username);
        List<Double> avgRatings = genreCounts.keySet().stream()
                .map(genre -> genreAvgRatings.getOrDefault(genre, 0.0))
                .collect(Collectors.toList());
        model.addAttribute("genreAverageRatings", avgRatings);

        // Monthly review count and watch time
        Map<YearMonth, Long> monthlyCount = statsService.getMonthlyReviewCount(username);
        Map<YearMonth, Integer> monthlyTime = statsService.getMonthlyWatchTime(username);

        // Format months for chart labels (e.g., "2025-08")
        List<String> monthLabels = monthlyCount.keySet().stream()
                .map(YearMonth::toString)
                .collect(Collectors.toList());

        List<Long> monthlyCounts = new ArrayList<>(monthlyCount.values());
        List<Integer> monthlyTimes = new ArrayList<>(monthlyTime.values());

        model.addAttribute("monthLabels", monthLabels);
        model.addAttribute("monthlyCounts", monthlyCounts);
        model.addAttribute("monthlyTimes", monthlyTimes);

        // Ratio of content types (e.g., movies vs TV shows)
        Map<String, Long> typeRatio = statsService.getTypeRatio(username);
        model.addAttribute("typeLabels", new ArrayList<>(typeRatio.keySet())); // e.g., ["movie", "tv"]
        model.addAttribute("typeData", new ArrayList<>(typeRatio.values()));   // e.g., [12, 8]

        return "mypage/stats"; // Return the view template for displaying stats
    }
}