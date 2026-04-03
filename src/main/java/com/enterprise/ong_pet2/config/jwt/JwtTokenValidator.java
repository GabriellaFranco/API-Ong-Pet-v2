package com.enterprise.ong_pet2.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtUtil.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            List<SimpleGrantedAuthority> authorities = Collections.emptyList();

            Object authClaim = claims.get("authorities");
            if (authClaim instanceof List<?> list) {
                authorities = list.stream()
                        .map(role -> {
                            String roleName = role.toString();
                            return new SimpleGrantedAuthority(
                                    roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName
                            );
                        })
                        .toList();
            }

            if (username != null) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(username, null, authorities)
                );
            }

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.warn("JWT inválido: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
