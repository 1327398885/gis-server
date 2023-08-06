package com.sun.gis.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 用于监听方法的执行时间
 */

@Aspect
@Component
public class ExecutionTimeAdvice {

    @Around("@annotation(com.sun.gis.aop.TrackExecutionTime)")
    public Object trackTime(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object obj = pjp.proceed();
        long endTime = System.currentTimeMillis();
        System.out.println("Method execution time: " + (endTime - startTime) + "ms");
        return obj;
    }
}
