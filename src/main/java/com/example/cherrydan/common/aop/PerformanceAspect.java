package com.example.cherrydan.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {
    @Around("@annotation(performanceMonitor)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint, PerformanceMonitor performanceMonitor) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();
            String keyword = args.length > 0 ? String.valueOf(args[0]) : "unknown";
            log.info("메서드: {}, 키워드: '{}', 실행시간: {}ms", methodName, keyword, stopWatch.getTotalTimeMillis());
        }
    }
} 