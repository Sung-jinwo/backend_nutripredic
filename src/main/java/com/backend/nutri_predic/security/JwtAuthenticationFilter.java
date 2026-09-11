package com.backend.nutri_predic.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final CustomUserDetailsService details;

    public JwtAuthenticationFilter(JwtService j, CustomUserDetailsService d) {
        jwt = j;
        details = d;
    }

    protected void doFilterInternal(HttpServletRequest r, HttpServletResponse s, FilterChain c)
            throws ServletException, IOException {
        String h = r.getHeader("Authorization");
        if (h != null
                && h.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null)
            try {
                var u = details.loadUserByUsername(jwt.subject(h.substring(7)));
                if (u.isEnabled()) {
                    var a = new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
                    a.setDetails(new WebAuthenticationDetailsSource().buildDetails(r));
                    SecurityContextHolder.getContext().setAuthentication(a);
                }
            } catch (JwtException | AuthenticationException | IllegalArgumentException ignored) {
            }
        c.doFilter(r, s);
    }
}
