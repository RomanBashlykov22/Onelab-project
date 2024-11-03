package kz.romanb.onelabproject.aop;

import jakarta.servlet.http.HttpServletRequest;
import kz.romanb.onelabproject.security.JwtAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllersMethods() {
    }

    @Before("restControllersMethods()")
    public void logRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String ipAddress = request.getRemoteAddr();
        String url = request.getRequestURL().toString();
        String httpMethod = request.getMethod();
        String username = getCurrentUsername();

        log.info("Запрос: HTTP {} - {}, IP: {}, Username: {}", httpMethod, url, ipAddress, username);
    }

    @AfterReturning(value = "restControllersMethods()", returning = "response")
    public void logResponse(Object response) {
        if (response instanceof ResponseEntity<?> responseEntity) {
            log.info("Ответ: Статус ответа - {}, Тело - {}", responseEntity.getStatusCode(), responseEntity.getBody());
        } else {
            log.info("Ответ: {}", response);
        }
    }

    private String getCurrentUsername() {
        Object auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthentication user) {
            return (String) user.getPrincipal();
        } else {
            return "Anonymous";
        }
    }
}
