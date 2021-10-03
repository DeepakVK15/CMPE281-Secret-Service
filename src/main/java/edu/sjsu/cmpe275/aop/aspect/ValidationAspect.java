package edu.sjsu.cmpe275.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

import java.util.*;

@Aspect
@Order(2)
public class ValidationAspect {
    /***
     *
     * Validates arguments of SecretService methods
     */
    private static String createdBy;
    private static String content;

    @Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.*(..))")
    public void dummyAdvice(JoinPoint joinPoint) {
        System.out.printf("Doing validation prior to the execution of the method %s\n", joinPoint.getSignature().getName());
    }

    @Before("args(userId, secretContent) && execution(public * createSecret(..)))")
    public void validateCreateSecret(String userId, String secretContent) {
        if (userId == null || secretContent == null || secretContent.length() > 128)
            throw new IllegalArgumentException();
        else {
            createdBy = userId;
            content = secretContent;
        }
    }

    @Before("args(userId, secretId, targetUserId) && execution(public * shareSecret(..)))")
    public void validateShareSecret(String userId, UUID secretId, String targetUserId) {
        if (userId == null || targetUserId == null || secretId == null || targetUserId.equals(userId))
            throw new IllegalArgumentException();
    }

    @Before("args(userId, secretId) && execution(public * readSecret(..)))")
    public void validateReadSecret(String userId, UUID secretId) {
        if (userId == null || secretId == null)
            throw new IllegalArgumentException();
    }

    @Before("args(userId, secretId,targetUserId) && execution(public * unshareSecret(..)))")
    public void validateUnShareSecret(String userId, UUID secretId, String targetUserId) {
        if (userId == null || targetUserId == null || secretId == null)
            throw new IllegalArgumentException();
    }

    public static String getCreatedBy() {
        return createdBy;
    }
    public static String getContent() { return content;}
}