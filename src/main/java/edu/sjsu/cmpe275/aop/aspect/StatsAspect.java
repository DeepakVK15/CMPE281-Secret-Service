package edu.sjsu.cmpe275.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import edu.sjsu.cmpe275.aop.SecretStatsImpl;

import java.io.IOException;

@Aspect
@Order(3)
public class StatsAspect {
    /***
     * Following is a dummy implementation of this aspect.
     * You are expected to provide an actual implementation based on the requirements, including adding/removing advices as needed.
     */

//	@Autowired SecretStatsImpl stats;

//	@After("execution(public void edu.sjsu.cmpe275.aop.SecretService.*(..))")
//	public void dummyAfterAdvice(JoinPoint joinPoint) {
//		System.out.printf("After the execution of the method %s\n", joinPoint.getSignature().getName());
//		//stats.resetStats();
//	}
//
//	@Before("execution(public void edu.sjsu.cmpe275.aop.SecretService.*(..))")
//	public void dummyBeforeAdvice(JoinPoint joinPoint) {
//		System.out.printf("Doing stats before the execution of the method %s\n", joinPoint.getSignature().getName());
//	}

	@Around("execution(public * edu.sjsu.cmpe275.aop.SecretStats.*(..))")
	public  Object retryStatsService(ProceedingJoinPoint joinPoint) throws Throwable {
		Object result = null;
		int retry = 0;
		for(;;) {
			try {
				result = joinPoint.proceed();
				System.out.printf("Finished the execution of the method %s which returned %s\n", joinPoint.getSignature().getName(), result);
				return result;
			} catch (Exception e) {
				retry++;
				if (retry > 2 || !(e instanceof Exception)) {
					e.printStackTrace();
					System.out.printf("Aborted the execution of the method %s\n", joinPoint.getSignature().getName());
				}
				else{
					//1, 2
					System.out.printf("Execution failed because of an exception, retrying after 500ms.\n");
					Thread.sleep(500);
				}
			}
		}

	}

}
