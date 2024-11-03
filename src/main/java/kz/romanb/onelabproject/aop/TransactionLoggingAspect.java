package kz.romanb.onelabproject.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
@Slf4j
public class TransactionLoggingAspect {
    @Pointcut("@annotation(transactional)")
    public void transactionalMethods(Transactional transactional) {
    }

    @Around("transactionalMethods(transactional)")
    public Object aroundTransaction(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        log.debug("""
                        Начало транзакции в методе {}
                        Тразакция начинается с параметрами: {}
                        Время начала - {}
                        """,
                joinPoint.getSignature().getName(),
                transactional,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy"))
        );
        Object retVal = joinPoint.proceed();
        log.debug("Транзакция в методе {} успешно завершена", joinPoint.getSignature().getName());
        return retVal;
    }

    @AfterThrowing(value = "transactionalMethods(transactional)", throwing = "e")
    public void afterThrowingTransaction(JoinPoint joinPoint, Transactional transactional, Throwable e) {
        log.debug("Транзакция в методе {} завершилась ошибкой: {}", joinPoint.getSignature().getName(), e.getMessage());
    }
}
