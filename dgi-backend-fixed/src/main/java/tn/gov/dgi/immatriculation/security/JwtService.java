package tn.gov.dgi.immatriculation.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.gov.dgi.immatriculation.model.Role;
import tn.gov.dgi.immatriculation.model.Utilisateur;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}") // 24h par défaut
    private long expirationMs;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String genererToken(Utilisateur utilisateur) {
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + expirationMs);

        return Jwts.builder()
                .subject(utilisateur.getEmail())
                .claim("userId", utilisateur.getId().toString())
                .claim("role", utilisateur.getRole().name())
                .claim("contribuableId", utilisateur.getContribuableId() != null
                        ? utilisateur.getContribuableId().toString() : null)
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(getKey())
                .compact();
    }

    public Claims extraireClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    public String extraireEmail(String token) {
        return extraireClaims(token).getSubject();
    }

    public UUID extraireUserId(String token) {
        return UUID.fromString(extraireClaims(token).get("userId", String.class));
    }

    public Role extraireRole(String token) {
        return Role.valueOf(extraireClaims(token).get("role", String.class));
    }

    public boolean estValide(String token) {
        try {
            return extraireClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}