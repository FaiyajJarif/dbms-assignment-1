package com.Eqinox.store.controllers;

import com.Eqinox.store.dtos.ApiResponse;
import com.Eqinox.store.dtos.LoginRequest;
import com.Eqinox.store.dtos.ResendVerificationRequest;
import com.Eqinox.store.dtos.SignupRequest;
import com.Eqinox.store.entities.EmailVerificationToken;
import com.Eqinox.store.entities.User;
import com.Eqinox.store.repositories.EmailVerificationTokenRepository;
import com.Eqinox.store.repositories.UserRepository;
import com.Eqinox.store.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {

        String email = request.getEmail();
        String password = request.getPassword();

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.failure("Invalid email or password"));
        }

        User user = userOpt.get();

        if (user.getPasswordHash() == null ||
                !BCrypt.checkpw(password, user.getPasswordHash())) {

            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.failure("Invalid email or password"));
        }

        if (Boolean.FALSE.equals(user.getIsVerified())) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.failure("Please verify your email before logging in."));
        }

        // TODO: add session / JWT later
        return ResponseEntity.ok(ApiResponse.success("Login successful"));
    }

    // ---------- SIGNUP (REST) ----------
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@RequestBody SignupRequest req) {

        String name = req.getName();
        String email = req.getEmail();
        String password = req.getPassword();
        String confirmPassword = req.getConfirmPassword();
        String dateOfBirth = req.getDateOfBirth(); // yyyy-MM-dd
        String timezone = req.getTimezone();
        Integer budgetStartDay = req.getBudgetStartDay();

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Name, email and password are required"));
        }

        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Passwords do not match"));
        }

        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Email already registered"));
        }

        // Hash password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Build user entity
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);

        // Optional fields
        if (dateOfBirth != null && !dateOfBirth.isBlank()) {
            user.setDateOfBirth(LocalDate.parse(dateOfBirth));
        }

        if (timezone != null && !timezone.isBlank()) {
            user.setTimezone(timezone);
        }

        if (budgetStartDay == null || budgetStartDay < 1 || budgetStartDay > 28) {
            user.setBudgetStartDay(1);
        } else {
            user.setBudgetStartDay(budgetStartDay);
        }

        // 5. Set defaults
        user.setRole("NORMAL_USER");
        user.setSubscriptionId(1); // free trial (ensure exists)
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        user.setIsVerified(false);

        // 6. Save to DB
        User savedUser = userRepository.save(user);

        // 7. Create verification token (valid for 24h)
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUserId(savedUser.getUserId());
        verificationToken.setToken(token);
        verificationToken.setExpiresAt(OffsetDateTime.now().plusDays(1));

        tokenRepository.save(verificationToken);

        // Build verification link (adjust port if needed)
        String verificationLink = "http://localhost:8080/verify-email?token=" + token;

        System.out.println("Verification link for " + savedUser.getEmail() + ": " + verificationLink);

        // Send email (ignore failure for now)
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), verificationLink);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(
                ApiResponse.success("Account created. Please check your email to verify your account."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse> resendVerification(@RequestBody ResendVerificationRequest req) {

        String email = req.getEmail();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Email is required"));
        }

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("No account found with this email"));
        }

        var user = userOpt.get();

        // Already verified?
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("This email is already verified. You can log in."));
        }

        // (Optional) clean up old tokens for this user
        var tokens = tokenRepository.findByUserId(user.getUserId());
        for (var t : tokens) {
            t.setUsed(true);
        }
        tokenRepository.saveAll(tokens);

        // Create new token
        String token = java.util.UUID.randomUUID().toString();
        var verificationToken = new EmailVerificationToken();
        verificationToken.setUserId(user.getUserId());
        verificationToken.setToken(token);
        verificationToken.setExpiresAt(OffsetDateTime.now().plusDays(1));
        verificationToken.setUsed(false);

        tokenRepository.save(verificationToken);

        // Build verification link (using 8080)
        String verificationLink = "http://localhost:8080/verify-email?token=" + token;
        System.out.println("Resent verification link for " + user.getEmail() + ": " + verificationLink);

        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationLink);
        } catch (Exception e) {
            System.out.println("❌ Failed to resend verification email:");
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("Could not send verification email. Try again later."));
        }

        return ResponseEntity.ok(
                ApiResponse.success("Verification email sent again. Please check your inbox."));
    }

}
