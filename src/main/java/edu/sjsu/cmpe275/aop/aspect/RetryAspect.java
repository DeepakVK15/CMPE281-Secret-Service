package edu.sjsu.cmpe275.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Aspect
@Order(1)
public class RetryAspect {
    /***
     * Following is a dummy implementation of this aspect.
     * You are expected to provide an actual implementation based on the requirements, including adding/removing advices as needed.
     */

    @Around("execution(public * edu.sjsu.cmpe275.aop.SecretService.*(..))")
	public Object retrySecretService(ProceedingJoinPoint joinPoint) throws Throwable {
		Object result = null;
		int retry = 0;
			for(;;) {
				try {
					result = joinPoint.proceed();
					System.out.printf("Finished the execution of the method %s \n", joinPoint.getSignature().getName());
					return result;
				} catch (Exception e) {
					retry++;
					if (retry <= 2 && (e instanceof IOException)) {
						System.out.printf("Execution failed because of IO exception, retrying after 500ms.\n");
						Thread.sleep(500);
					}
					else if(e instanceof IOException){
						e.printStackTrace();
						System.out.printf("Aborted the execution of the method %s\n", joinPoint.getSignature().getName());
						throw new IOException();
					}
					else{
						e.printStackTrace();
						System.out.printf("Aborted the execution of the method %s\n", joinPoint.getSignature().getName());
						break;
					}
				}
			}
			return null;
		}
}
