package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

@Component
public class TokenService {
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final String jwtSecret;
    private final SecretKey signingKey;

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
                .setClaims(claims)  // Claims hinzufügen
                .setSubject(subject) // E-Mail oder Benutzername
                .setIssuedAt(now)
                .setExpiration(expiryDate)
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
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null; // Token ungültig oder abgelaufen
        }
    }

    // --- Claims aus Token extrahieren ---
    public Map<String, String> extractClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return new HashMap<>(claims); // Alle Claims als Map
        } catch (Exception e) {
            return null;
        }
    }

    // --- Token validieren (mit Rolle) ---
    public boolean validateToken(String token, String role) {
        try {
            String email = extractEmail(token);
            if (email == null) return false;

            switch (role.toLowerCase()) {
                case "admin":
                    return adminRepository.findByEmail(email).isPresent();
                case "doctor":
                    return doctorRepository.findByEmail(email).isPresent();
                case "patient":
                    return patientRepository.findByEmail(email).isPresent();
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    // --- Token validieren (ohne Rolle) ---
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
