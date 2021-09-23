package edu.sjsu.cmpe275.aop.aspect;

import edu.sjsu.cmpe275.aop.SecretStatsImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

import java.util.*;

@Aspect
@Order(1)
public class ValidationAspect {
    /***
     * Following is a dummy implementation of this aspect.
     * You are expected to provide an actual implementation based on the requirements, including adding/removing advices as needed.
     */
    private static String id;

    @Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.*(..))")
    public void dummyAdvice(JoinPoint joinPoint) {
        System.out.printf("Doing validation prior to the execution of the method %s\n", joinPoint.getSignature().getName());
    }

    @Before("args(userId, secretContent) && execution(public * createSecret(..)))")
    public void validateCreateSecret(String userId, String secretContent) {
        System.out.println("validate creation");
        if (userId == null || userId.length() == 0 || secretContent == null || secretContent.length() > 128)
            throw new IllegalArgumentException();
        else {
            id = userId;
            if(secretContent.length()> SecretStatsImpl.lengthOfLongestSecret)
                SecretStatsImpl.lengthOfLongestSecret = secretContent.length();
        }
    }

    @Before("args(userId, secretId, targetUserId) && execution(public * shareSecret(..)))")
    public void validateShareSecret(String userId, UUID secretId, String targetUserId) {
        System.out.println("validate sharing");

        if (userId == null || userId.length() == 0 || targetUserId == null || targetUserId.length() == 0 || secretId == null || targetUserId.equals(userId))
            throw new IllegalArgumentException();
    }

    @Before("args(userId, secretId) && execution(public * readSecret(..)))")
    public void validateReadSecret(String userId, UUID secretId) {
        System.out.println("validate read");
        if (userId == null || userId.length() == 0 || secretId == null)
            throw new IllegalArgumentException();
    }

    @Before("args(userId, secretId,targetUserId) && execution(public * unshareSecret(..)))")
    public void validateUnShareSecret(String userId, UUID secretId, String targetUserId) {
        if (userId == null || targetUserId == null || secretId == null || userId.equals(targetUserId))
            throw new IllegalArgumentException();
    }

    public static String getId() {
        return id;
    }
}