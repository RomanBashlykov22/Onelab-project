package kz.romanb.onelabproject.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.romanb.onelabproject.exceptions.AuthException;
import kz.romanb.onelabproject.models.dto.ErrorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {
    private static final String AUTHORIZATION = "Authorization";
    private final JwtProvider jwtProvider;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final String token = getTokenFromRequest((HttpServletRequest) servletRequest);

        try {
            if (token != null && jwtProvider.validateAccessToken(token)) {
                Claims claims = jwtProvider.getAccessClaims(token);
                JwtAuthentication jwtInfoToken = JwtUtils.generate(claims);
                jwtInfoToken.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(jwtInfoToken);
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (AuthException e) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    new ObjectMapper()
                            .writeValueAsString(
                                    ErrorDto.builder()
                                            .error("Unauthorized")
                                            .description(e.getMessage())
                                            .build()
                            ));
        }
    }

    private String getTokenFromRequest(HttpServletRequest servletRequest) {
        String bearer = servletRequest.getHeader(AUTHORIZATION);

        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
