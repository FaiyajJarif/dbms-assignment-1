package com.Eqinox.store.controllers;

import com.Eqinox.store.entities.Category;
import com.Eqinox.store.entities.User;
import com.Eqinox.store.repositories.CategoryRepository;
import com.Eqinox.store.repositories.UserRepository;
import com.Eqinox.store.security.JwtService;
import com.Eqinox.store.services.DashboardService;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final JwtService jwtService;
    private final DashboardService dashboardService;

    public DashboardController(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            JwtService jwtService,
            DashboardService dashboardService) {

        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.jwtService = jwtService;
        this.dashboardService = dashboardService;
    }

    // ================= LOGGED-IN USER =================
    @GetMapping("/me")
    public ResponseEntity<?> me(
            @RequestHeader("Authorization") String authHeader) {

        String email = jwtService.getEmail(authHeader.substring(7));
        User user = userRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "createdAt", user.getCreatedAt()));
    }

    // ================= TOTAL USERS (COUNT) =================
    @GetMapping("/total-users")
    public ResponseEntity<?> totalUsers() {
        return ResponseEntity.ok(
                Map.of("totalUsers", userRepository.countAllUsers()));
    }

    // ================= SEARCH USERS =================
    @GetMapping("/search-users")
    public ResponseEntity<?> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        List<User> users = userRepository.searchUsersStartingWithPaged(
                keyword,
                PageRequest.of(page, size));

        long total = userRepository.searchUsersStartingWith(keyword).size();

        return ResponseEntity.ok(Map.of(
                "users", users,
                "currentPage", page,
                "pageSize", size,
                "totalResults", total));
    }

    // ================= CATEGORIES =================
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(
            @RequestHeader("Authorization") String authHeader) {

        String email = jwtService.getEmail(authHeader.substring(7));
        User user = userRepository.findByEmail(email).orElseThrow();

        List<Category> categories = categoryRepository.findByUserId(user.getUserId());

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        for (Category c : categories) {
            result.computeIfAbsent(c.getType(), k -> new ArrayList<>())
                    .add(Map.of(
                            "id", c.getCategoryId(),
                            "name", c.getName()));
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/first-15-users")
    public ResponseEntity<?> first15Users() {

        List<User> users = userRepository.findFirst15ByRegistrationDateAsc(
                org.springframework.data.domain.PageRequest.of(0, 15));

        return ResponseEntity.ok(users);
    }

    @GetMapping("/user-categories")
    public ResponseEntity<?> getUserCategoriesByEmail(
            @RequestParam String email) {

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find categories for that user
        List<Category> categories = categoryRepository.findByUserId(user.getUserId());

        // Prepare response
        return ResponseEntity.ok(Map.of(
                "user", Map.of(
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "registeredAt", user.getCreatedAt()),
                "categories", categories));
    }

    // ================= USER GROWTH (LIVE CHART) =================

    @GetMapping("/user-growth")
    public ResponseEntity<?> userGrowth(
            @RequestParam(defaultValue = "daily") String type) {

        List<Object[]> userRaw;
        List<Object[]> categoryRaw;

        if ("weekly".equals(type)) {
            userRaw = userRepository.getWeeklyUserGrowth();
            categoryRaw = userRepository.getDailyCategoryGrowthViaUsers(); // weekly approx later
        } else {
            userRaw = userRepository.getDailyUserGrowth();
            categoryRaw = userRepository.getDailyCategoryGrowthViaUsers();
        }

        List<String> labels = new ArrayList<>();
        List<Long> userValues = new ArrayList<>();
        List<Long> categoryValues = new ArrayList<>();

        // Build user dataset
        for (Object[] r : userRaw) {
            labels.add(r[0].toString());
            userValues.add(((Number) r[1]).longValue());
        }

        // Build category dataset
        for (Object[] r : categoryRaw) {
            categoryValues.add(((Number) r[1]).longValue());
        }

        return ResponseEntity.ok(Map.of(
                "labels", labels,
                "users", userValues,
                "categories", categoryValues));
    }

}
