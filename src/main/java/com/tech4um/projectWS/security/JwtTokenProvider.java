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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Adicionado
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority; // Adicionado
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Adicionado
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.slf4j.Logger; // Adicionado
import org.slf4j.LoggerFactory; // Adicionado

import java.security.Key;
import java.util.Arrays; // Adicionado
import java.util.Collection; // Adicionado
import java.util.Date;
import java.util.stream.Collectors; // Adicionado

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class); // Logging profissional

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

        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();

        Date now  = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // Supondo que você inclua roles no seu token (necessário para o método getAuthentication)
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String jwt = Jwts.builder()
                .setSubject(userEmail)
                .claim("roles", roles) // Inclui as roles (importante para getAuthentication)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    /**
     * CRÍTICO PARA WEBSOCKET/STOMP:
     * Este método extrai as Claims (Subject e Roles) do token e constrói um objeto Authentication
     * para ser anexado à sessão do WebSocket.
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // 1. Extrai as roles do token (supondo que você as inclua como Claims, como no generateToken acima)
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("roles").toString().split(","))
                        .filter(role -> !role.trim().isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // 2. Cria o objeto principal (simplesmente usando o Subject como nome do usuário)
        UserDetails principal = new UserDetailsAdapter(claims.getSubject(), authorities);

        // 3. Retorna o objeto de autenticação
        // O token é passado como credencial (opcional, mas útil para debug)
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // Classe de Adaptador interna para UserDetails simples (substituindo o antigo org.springframework.security.core.userdetails.User)
    private static class UserDetailsAdapter implements UserDetails {
        private final String username;
        private final Collection<? extends GrantedAuthority> authorities;

        public UserDetailsAdapter(String username, Collection<? extends GrantedAuthority> authorities) {
            this.username = username;
            this.authorities = authorities;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
        @Override
        public String getPassword() { return null; } // N/A for JWT principal
        @Override
        public String getUsername() { return username; }
        @Override
        public boolean isAccountNonExpired() { return true; }
        @Override
        public boolean isAccountNonLocked() { return true; }
        @Override
        public boolean isCredentialsNonExpired() { return true; }
        @Override
        public boolean isEnabled() { return true; }
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
            logger.error("JWT ERROR: Assinatura inválida! Chave Secreta errada ou Token adulterado.");
        } catch (MalformedJwtException ex) {
            logger.error("JWT ERROR: Token malformado.");
        } catch (ExpiredJwtException ex) {
            logger.error("JWT ERROR: Token expirado em: {}", ex.getClaims().getExpiration());
        } catch (UnsupportedJwtException ex) {
            logger.error("JWT ERROR: Token não suportado.");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT ERROR: O argumento (token) está vazio.");
        }
        return false;
    }
}