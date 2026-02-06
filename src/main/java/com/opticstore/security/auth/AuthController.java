package com.opticstore.security.auth;

import com.opticstore.security.auth.dto.LoginRequest;
import com.opticstore.security.auth.dto.LoginResponse;
import com.opticstore.security.auth.dto.LogoutResponse;
import com.opticstore.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Add this

    // In-memory token blacklist
    private final Map<String, LocalDateTime> tokenBlacklist = new ConcurrentHashMap<>();

    // Update constructor to include UserDetailsService
    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserDetailsService userDetailsService // Add this parameter
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService; // Initialize
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()
                        )
                );

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Add token to blacklist with expiry time
                java.util.Date expiryDate = jwtService.extractExpiration(token);
                LocalDateTime expiryTime = expiryDate.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();

                tokenBlacklist.put(token, expiryTime);

                // Clean up expired tokens
                cleanUpBlacklist();

                // Clear any cookies if using them
                clearAuthCookies(response);

                return ResponseEntity.ok(
                        new LogoutResponse("Logged out successfully", true)
                );

            } catch (Exception e) {
                // Even if token is invalid, we consider logout successful
                return ResponseEntity.ok(
                        new LogoutResponse("Logged out (token was invalid)", true)
                );
            }
        }

        // No token provided
        return ResponseEntity.ok(
                new LogoutResponse("No active session found", true)
        );
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("valid", false);
            response.put("message", "No token provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.substring(7);

        // Check if token is blacklisted
        if (tokenBlacklist.containsKey(token)) {
            response.put("valid", false);
            response.put("message", "Token has been invalidated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String username = jwtService.extractUsername(token);
            response.put("valid", true);
            response.put("username", username);
            response.put("roles", jwtService.extractRoles(token));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("No refresh token provided");
        }

        String refreshToken = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(refreshToken);

            // Load user details using the injected service
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Generate new access token
            String newToken = jwtService.generateToken(userDetails);

            // Add old token to blacklist
            java.util.Date expiryDate = jwtService.extractExpiration(refreshToken);
            LocalDateTime expiryTime = expiryDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
            tokenBlacklist.put(refreshToken, expiryTime);

            return ResponseEntity.ok(new LoginResponse(newToken));

        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "No token provided")
            );
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", userDetails.getUsername());
            userInfo.put("authorities", userDetails.getAuthorities());
            userInfo.put("enabled", userDetails.isEnabled());

            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Invalid token")
            );
        }
    }

    private void clearAuthCookies(HttpServletResponse response) {
        // Clear any authentication cookies
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("auth_token", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void cleanUpBlacklist() {
        LocalDateTime now = LocalDateTime.now();
        tokenBlacklist.entrySet().removeIf(entry ->
                entry.getValue().isBefore(now)
        );
    }

    @GetMapping("/test-token")
    public ResponseEntity<String> testToken() {
        try {
            // Create a test user
            UserDetails testUser = new org.springframework.security.core.userdetails.User(
                    "test",
                    "password",
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );

            // Generate token
            String token = jwtService.generateToken(testUser);

            // Extract info
            String username = jwtService.extractUsername(token);
            List<String> roles = jwtService.extractRoles(token);
            Date expiry = jwtService.extractExpiration(token);

            return ResponseEntity.ok(
                    String.format("Token generated successfully!\n" +
                                    "Username: %s\n" +
                                    "Roles: %s\n" +
                                    "Expires: %s\n" +
                                    "Token: %s...",
                            username, roles, expiry, token.substring(0, 50))
            );

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error: " + e.getMessage());
        }
    }


    @GetMapping("/debug-token")
    public ResponseEntity<Map<String, Object>> debugToken(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        String authHeader = request.getHeader("Authorization");
        response.put("hasAuthHeader", authHeader != null);

        if (authHeader != null) {
            response.put("authHeader", authHeader);
            response.put("startsWithBearer", authHeader.startsWith("Bearer "));
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            response.put("tokenLength", token.length());

            try {
                String username = jwtService.extractUsername(token);
                response.put("username", username);

                List<String> roles = jwtService.extractRoles(token);
                response.put("roles", roles);

                // Decode token manually
                String[] parts = token.split("\\.");
                if (parts.length == 3) {
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                    response.put("payload", payload);

                    // Also parse as JSON for easier reading
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> payloadMap = mapper.readValue(payload, Map.class);
                    response.put("parsedPayload", payloadMap);
                }

                // Check if token is valid
                boolean isValid = jwtService.isTokenValid(token);
                response.put("tokenValid", isValid);

                // Check expiration
                Date expiry = jwtService.extractExpiration(token);
                response.put("expiresAt", expiry);
                response.put("isExpired", expiry.before(new Date()));

            } catch (Exception e) {
                response.put("error", e.getMessage());
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(response);
    }


}