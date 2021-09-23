package edu.sjsu.cmpe275.aop.aspect;

import edu.sjsu.cmpe275.aop.NotAuthorizedException;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

import java.util.*;

@Aspect
@Order(1)
public class AccessControlAspect {
    /***
     * Following is a dummy implementation of this aspect.
     * You are expected to provide an actual implementation based on the requirements, including adding/removing advices as needed.
     */
    private  final Map<UUID, List<String>> sharedAndUnReadSecrets = new HashMap<>();

    private  final Map<UUID, List<String>> authorisedUsers = new HashMap<>();

    @AfterReturning(pointcut = "execution(public * createSecret(..))", returning = "id")
    public void storeSecret(UUID id) {
        authorisedUsers.put(id, new ArrayList<>());
        authorisedUsers.get(id).add(ValidationAspect.getId());
    }

    @Before("args(userId, secretId, targetUserId) && execution(public * shareSecret(..)))")
    public void validateShareSecret(String userId, UUID secretId, String targetUserId) {
        System.out.println("validate sharing");

        List<String> authorisedUsers = this.authorisedUsers.get(secretId);

        if (authorisedUsers == null || !authorisedUsers.contains(userId))
            throw new NotAuthorizedException();

        if (!sharedAndUnReadSecrets.containsKey(secretId))
            sharedAndUnReadSecrets.put(secretId, new ArrayList<>());

        sharedAndUnReadSecrets.get(secretId).add(targetUserId);
    }

    @Before("args(userId, secretId) && execution(public * readSecret(..)))")
    public void validateReadSecret(String userId, UUID secretId) {
        System.out.println("validate read");
        List<String> userListForSecret = authorisedUsers.get(secretId);

        if (userListForSecret.contains(userId))
            return;

        List<String> shared = sharedAndUnReadSecrets.get(secretId);

        if (shared == null || !shared.contains(userId))
            throw new NotAuthorizedException();
        else {
            shared.remove(userId);
            sharedAndUnReadSecrets.put(secretId, shared);
            authorisedUsers.get(secretId).add(userId);
        }
    }

    @Before("args(userId, secretId,targetUserId) && execution(public * unshareSecret(..)))")
    public void validateUnShareSecret(String userId, UUID secretId, String targetUserId) {
        List<String> userListForSecret = authorisedUsers.get(secretId);
        if (userListForSecret == null || !userListForSecret.contains(targetUserId) || !userListForSecret.get(0).equals(userId))
            throw new NotAuthorizedException();
    }
}
