package ru.practicum.shareit.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(public * ru.practicum.shareit.user.UserController.*(..))" +
            " || execution(public * ru.practicum.shareit.item.ItemController.*(..))")
    private void publicMethodsControllers() {
    }

    @Before(value = "publicMethodsControllers()")
    public void logBefore(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().toShortString();
        log.info(">> {}, args: {}", methodName, Arrays.toString(args));
    }

    @AfterReturning(value = "publicMethodsControllers()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("<< {}, result: {}", methodName, result);
    }
}