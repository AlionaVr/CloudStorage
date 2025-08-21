package com.example.securitylib;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MyJwtFilter extends OncePerRequestFilter {
    @Value("${security.jwt.header}")
    private String header;

    private final JwtService jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (isPublicEndpoint(path)) {
            chain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                var claims = jwt.parse(token).getBody();
                String username = claims.getSubject();
                @SuppressWarnings("unchecked")
                var roles = (List<String>) claims.getOrDefault("roles", List.of());

                var auth = new UsernamePasswordAuthenticationToken(
                        username, null,
                        roles.stream().map(SimpleGrantedAuthority::new).toList());

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Invalid or expired token\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest req) {
        String token = req.getHeader(header);// "auth-token"
        if (StringUtils.hasText(token))
            return token;

        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String bearerToken = authHeader.substring(7);
            return StringUtils.hasText(bearerToken) ? bearerToken : null;
        }
        return null;
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/cloud/login") ||
                path.equals("/cloud/logout");
    }
}