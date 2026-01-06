package com.project.back_end.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenService {
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SecretKey signingKey;
    private final String jwtSecret;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository,
                        @Value("${jwt.secret}") String jwtSecret) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.jwtSecret = jwtSecret;
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // --- Token mit Claims generieren ---
    public String generateToken(String subject, Map<String, String> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 604800000L); // 7 Tage gültig

        return Jwts.builder()
                .claims(claims)      // Neu: .addClaims() statt .setClaims()
                .subject(subject)       // Neu: .subject() statt .setSubject()
                .issuedAt(now)          // Neu: .issuedAt() statt .setIssuedAt()
                .expiration(expiryDate)  // Neu: .expiration() statt .setExpiration()
                .signWith(signingKey)
                .compact();
    }


    // --- Überladene Methode für Kompatibilität ---
    public String generateToken(String subject) {
        return generateToken(subject, new HashMap<>()); // Leere Claims
    }

    // --- E-Mail aus Token extrahieren ---
    public String extractEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null; // Token ungültig oder abgelaufen
        }
    }

    // --- Claims aus Token extrahieren ---
    public Map<String, String> extractClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Map<String, String> map = new HashMap<>(); // Alle Claims als Map
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                map.put(entry.getKey(), entry.getValue().toString());
            }
            return map;
        } catch (Exception e) {
            return null;
        }
    }

    // --- Token validieren (mit Rolle) ---
    public boolean validateToken(String token, String role) {
        try {
            String email = extractEmail(token);
            if (email == null) return false;

            return switch (role.toLowerCase()) {
                case "admin" -> adminRepository.findByEmail(email).isPresent();
                case "doctor" -> doctorRepository.findByEmail(email).isPresent();
                case "patient" -> patientRepository.findByEmail(email).isPresent();
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    // --- Token validieren (ohne Rolle) ---
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
