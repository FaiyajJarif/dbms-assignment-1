package com.Eqinox.store.controllers;

import com.Eqinox.store.dtos.PlanBudgetRequest;
import com.Eqinox.store.entities.BudgetPeriod;
import com.Eqinox.store.entities.User;
import com.Eqinox.store.repositories.UserRepository;
import com.Eqinox.store.security.JwtService;
import com.Eqinox.store.services.BudgetService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService budgetService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public BudgetController(
            BudgetService budgetService,
            JwtService jwtService,
            UserRepository userRepository) {
        this.budgetService = budgetService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // ✅ Get current month budget
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentBudget(
            @RequestHeader("Authorization") String authHeader) {

        String email = jwtService.getEmail(authHeader.substring(7));
        User user = userRepository.findByEmail(email).orElseThrow();

        BudgetPeriod period =
                budgetService.getOrCreateCurrentBudget(user.getUserId());

        budgetService.syncBudgetItems(period);

        return ResponseEntity.ok(period);
    }

    // ✅ SAVE budget amount for category
    @PostMapping("/plan")
    public ResponseEntity<?> planBudget(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PlanBudgetRequest request) {

        String email = jwtService.getEmail(authHeader.substring(7));
        User user = userRepository.findByEmail(email).orElseThrow();

        BudgetPeriod period =
                budgetService.getOrCreateCurrentBudget(user.getUserId());

        budgetService.planAmount(
                period.getBudgetId(),
                request.getCategoryId(),
                request.getAmount()
        );

        return ResponseEntity.ok().build();
    }
}
