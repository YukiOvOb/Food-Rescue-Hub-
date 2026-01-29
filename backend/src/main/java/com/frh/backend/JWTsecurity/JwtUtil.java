// package com.frh.backend.JWTsecurity;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Component;

// import java.security.Key;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.function.Function;

// @Component
// public class JwtUtil {

// @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256Algorithm}")
// // it acts a secret key to
// // sign our JWT
// private String secret;

// // 24 hours validation of token (alsyws we shoul mention in milli sec), as
// the
// // default is 1 day
// @Value("${jwt.expiration:86400000}")
// private Long expiration;

// // Converts your secret string into a key that JWT library can use to sign
// the
// // token
// private Key getSigningKey() {
// return Keys.hmacShaKeyFor(secret.getBytes());
// }

// // Creates a token for a user (using their email)
// public String generateToken(String email) {
// Map<String, Object> claims = new HashMap<>();
// return createToken(claims, email);
// }

// private String createToken(Map<String, Object> claims, String subject) {
// return Jwts.builder()
// .setClaims(claims)
// .setSubject(subject)
// .setIssuedAt(new Date(System.currentTimeMillis()))
// .setExpiration(new Date(System.currentTimeMillis() + expiration))
// .signWith(getSigningKey(), SignatureAlgorithm.HS256)
// .compact();
// }

// // to get the email out of the token
// public String extractEmail(String token) {
// return extractClaim(token, Claims::getSubject);
// }

// // to get the expiration date from the token
// public Date extractExpiration(String token) {
// return extractClaim(token, Claims::getExpiration);
// }

// // to get any info from the token -- email, expiration, roles, etc
// public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
// final Claims claims = extractAllClaims(token);
// return claimsResolver.apply(claims);
// }

// // Reads all the data inside the token
// private Claims extractAllClaims(String token) {
// return Jwts.parserBuilder()
// .setSigningKey(getSigningKey())
// .build()
// .parseClaimsJws(token)
// .getBody();
// }

// // Checks if the token is already expired
// private Boolean isTokenExpired(String token) {
// return extractExpiration(token).before(new Date());
// }

// // tok validation
// public Boolean validateToken(String token, String email) {
// final String tokenEmail = extractEmail(token);
// return (tokenEmail.equals(email) && !isTokenExpired(token));
// }
// }