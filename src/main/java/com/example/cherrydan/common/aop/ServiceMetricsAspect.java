package com.example.cherrydan.common.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceMetricsAspect {

    private static final long SLOW_THRESHOLD_MS = 500;

    private final MeterRegistry meterRegistry;

    @Around("@within(org.springframework.stereotype.Service) && " +
            "execution(public * *(..)) && " +
            "!execution(* get*()) && " +
            "!execution(* is*()) && " +
            "!execution(* has*())")
    public Object measureServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            if (executionTime > SLOW_THRESHOLD_MS) {
                log.warn("느린 서비스 메서드 감지: {}.{} - {}ms 소요",
                        className, methodName, executionTime);
            }

            Timer.builder("service.method.execution")
                    .tag("service", className)
                    .tag("method", methodName)
                    .description("Service method execution time")
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);
        }
    }
}
