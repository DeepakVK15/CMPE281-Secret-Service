package edu.sjsu.cmpe275.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Aspect
@Order(1)
public class RetryAspect {
    /***
     *Retries utmost twice in case of IO exception for SecretService Methods
     */

    @Around("execution(public * edu.sjsu.cmpe275.aop.SecretService.*(..))")
    public Object retrySecretService(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        int retry = 0;
        for (; ; ) {
            try {
                result = joinPoint.proceed();
                System.out.printf("Finished the execution of the method %s \n", joinPoint.getSignature().getName());
                return result;
            } catch (Exception e) {
                retry++;
                if (retry <= 2 && (e instanceof IOException)) {
                    System.out.print("Execution failed because of IO exception, retrying after 500ms.\n");
                    Thread.sleep(500);
                } else if (e instanceof IOException) {
                    e.printStackTrace();
                    System.out.printf("Aborted the execution of the method %s\n", joinPoint.getSignature().getName());
                    throw new IOException();
                } else {
                    e.printStackTrace();
                    System.out.printf("Aborted the execution of the method %s\n", joinPoint.getSignature().getName());
                    break;
                }
            }
        }
        return null;
    }
}
