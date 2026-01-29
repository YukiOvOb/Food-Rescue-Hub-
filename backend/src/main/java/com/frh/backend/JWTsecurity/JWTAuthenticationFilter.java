// package com.frh.backend.JWTsecurity;

// import com.frh.backend.repository.SupplierProfileRepository;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;

// import lombok.extern.slf4j.Slf4j;

// import
// org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import
// org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;
// import java.util.List;

// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class JWTAuthenticationFilter extends OncePerRequestFilter {

// private final JwtUtil jwtUtil;
// private final SupplierProfileRepository supplierRepository;

// @Override
// protected void doFilterInternal(
// HttpServletRequest request,
// HttpServletResponse response,
// FilterChain filterChain) throws ServletException, IOException {

// final String authHeader = request.getHeader("Authorization");

// // If no Authorization header or not Bearer token → continue
// if (authHeader == null || !authHeader.startsWith("Bearer ")) {
// filterChain.doFilter(request, response);
// return;
// }

// try {
// // Extract JWT token
// String jwt = authHeader.substring(7);
// String userEmail = jwtUtil.extractEmail(jwt);

// // Authenticate only if not already authenticated
// if (userEmail != null &&
// SecurityContextHolder.getContext().getAuthentication() == null) {

// var supplier = supplierRepository.findByEmail(userEmail).orElse(null);

// // Validate token
// if (supplier != null && jwtUtil.validateToken(jwt, userEmail)) {

// UsernamePasswordAuthenticationToken authToken = new
// UsernamePasswordAuthenticationToken(
// userEmail,
// null,
// List.of() // no roles for now
// );

// authToken.setDetails(
// new WebAuthenticationDetailsSource()
// .buildDetails(request));

// SecurityContextHolder.getContext()
// .setAuthentication(authToken);

// log.debug("JWT authenticated user: {}", userEmail);
// }
// }
// } catch (Exception e) {
// log.error("JWT authentication failed: {}", e.getMessage());
// }

// filterChain.doFilter(request, response);
// }
// }
