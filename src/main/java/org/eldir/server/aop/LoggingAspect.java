package org.eldir.server.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // Определяем точку среза: все методы в пакете service
    @Pointcut("execution(* org.eldir.server.service..*(..))")
    public void serviceMethods() {}

    @Around("serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        log.info(">>> EXECUTING {}.{} with args: {}", className, methodName, Arrays.toString(args));

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed(); // Выполняем реальный метод

            long executionTime = System.currentTimeMillis() - start;
            log.info("<<< FINISHED {}.{} in {} ms. Result: {}", className, methodName, executionTime, result);

            return result;
        } catch (Throwable e) {
            log.error("!!! EXCEPTION in {}.{}: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}