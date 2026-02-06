package com.opticstore.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        System.out.println("=== JWT FILTER DEBUG ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Authorization header present: " + (authHeader != null));

        if (authHeader != null) {
            System.out.println("Auth header starts with Bearer: " + authHeader.startsWith("Bearer "));
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Bearer token, skipping filter");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username;

        try {
            username = jwtService.extractUsername(token);
            System.out.println("Extracted username: " + username);

            // Debug: print roles from token
            List<String> roles = jwtService.extractRoles(token);
            System.out.println("Roles from token: " + roles);

        } catch (Exception e) {
            System.out.println("Failed to extract username from token: " + e.getMessage());
            e.printStackTrace();
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            System.out.println("Loaded user details username: " + userDetails.getUsername());
            System.out.println("Loaded user details authorities: " + userDetails.getAuthorities());

            if (jwtService.isTokenValid(token, userDetails)) {
                System.out.println("Token is valid");

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                System.out.println("Setting authentication with authorities: " + authToken.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Verify authentication was set
                Authentication setAuth = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("Authentication set in context: " + (setAuth != null));
                if (setAuth != null) {
                    System.out.println("Context authorities: " + setAuth.getAuthorities());
                }
            } else {
                System.out.println("Token invalid for user");
            }
        } else {
            System.out.println("Username null or already authenticated");
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                System.out.println("Already authenticated with: " +
                        SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            }
        }

        filterChain.doFilter(request, response);
    }
}
