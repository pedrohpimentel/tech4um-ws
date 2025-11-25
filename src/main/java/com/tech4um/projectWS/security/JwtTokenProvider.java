package com.tech4um.projectWS.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // A chave secreta será lida do application.properties
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    // Para obter a chave de assinatura
    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Gera o token JWT após o Login
    public String generateToken(Authentication authentication){

        // 💡 Ajuste: Obtém o username (email) do objeto UserDetails, que é o Principal
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();

        Date now  = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        System.out.println("PRINT ANTES JWT");
        String jwt = Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
        System.out.println("PRINT DEPOIS JWT" + jwt);
        return jwt;
    }

    // Obtém o email (subject) do Token
    public String getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // Valida o Token
    public boolean validateToken(String authToken){
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (SignatureException ex) {
            // 🛑 ERRO MAIS CRÍTICO: CHAVE INVÁLIDA
            System.err.println("JWT ERROR: Assinatura inválida! Chave Secreta errada ou Token adulterado.");
        } catch (MalformedJwtException ex) {
            System.err.println("JWT ERROR: Token malformado.");
        } catch (ExpiredJwtException ex) {
            // 🛑 ERRO CRÍTICO: TOKEN EXPIRADO
            System.err.println("JWT ERROR: Token expirado em: " + ex.getClaims().getExpiration());
        } catch (UnsupportedJwtException ex) {
            System.err.println("JWT ERROR: Token não suportado.");
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT ERROR: O argumento (token) está vazio.");
        }
        return false;
    }
}