package edu.sjsu.cmpe275.aop.aspect;

import edu.sjsu.cmpe275.aop.NotAuthorizedException;
import edu.sjsu.cmpe275.aop.SecretStatsImpl;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

import java.util.*;

@Aspect
@Order(3)
public class AccessControlAspect {
    /***
     * Following is a dummy implementation of this aspect.
     * You are expected to provide an actual implementation based on the requirements, including adding/removing advices as needed.
     */
    public class Secret {
        private final Set<String> authorisedUsers;
        private final Set<String> sharedWith;
        private final String createdBy;
        private final String content;
        private final Set<String> readBy;

        public Secret(UUID id, String createdBy, String content) {
            this.createdBy = createdBy;
            this.content = content;
            this.authorisedUsers = new HashSet<>();
            this.authorisedUsers.add(createdBy);
            this.sharedWith = new HashSet<>();
            this.readBy = new HashSet<>();
        }

        public Set<String> getReadBy() {
            return this.readBy;
        }

        public String getContent() {
            return this.content;
        }

    }

    public class User {
        private String name;
        private int trustWorthyScore;
        private int secretKeeperScore;

        public User(String name) {
            this.name = name;
            trustWorthyScore = 0;
            secretKeeperScore = 0;
        }

        public int getTrustWorthyScore() {
            return trustWorthyScore;
        }

        public int getSecretKeeperScore() {
            return secretKeeperScore;
        }

    }

    public static final Map<UUID, Secret> secrets = new HashMap<>();
    public static final Map<String, User> users = new HashMap<>();

    @AfterReturning(pointcut = "execution(public * createSecret(..))", returning = "id")
    public void storeSecret(UUID id) {
        Secret secret = new Secret(id, ValidationAspect.getCreatedBy(), ValidationAspect.getContent());
        secrets.put(id, secret);

        if(secret.getContent().length() > SecretStatsImpl.lengthOfLongestSecret)
            SecretStatsImpl.lengthOfLongestSecret = secret.getContent().length();

        if (!users.containsKey(secret.createdBy))
            users.put(secret.createdBy, new User(secret.createdBy));
    }

    @Before("args(userId, secretId, targetUserId) && execution(public * shareSecret(..)))")
    public void validateShareSecret(String userId, UUID secretId, String targetUserId) {
        Set<String> authorisedUsers = secrets.get(secretId).authorisedUsers;
        if (!secrets.containsKey(secretId) || !authorisedUsers.contains(userId))
            throw new NotAuthorizedException();
        else {
            if (!userId.equals(secrets.get(secretId).createdBy)) {
                users.get(userId).secretKeeperScore++;
            }
            if (!users.containsKey(targetUserId)) {
                User user = new User(targetUserId);
                users.put(targetUserId, user);
            }
            users.get(targetUserId).trustWorthyScore++;
            secrets.get(secretId).sharedWith.add(targetUserId);
        }
    }

    @Before("args(userId, secretId) && execution(public * readSecret(..)))")
    public void validateReadSecret(String userId, UUID secretId) {
        if (!secrets.containsKey(secretId))
            throw new NotAuthorizedException();

        Secret secret = secrets.get(secretId);
        if (secret.authorisedUsers.contains(userId) || secret.sharedWith.contains(userId)) {
            if (!userId.equals(secret.createdBy)) {
                secret.authorisedUsers.add(userId);
                secret.sharedWith.remove(userId);
                secret.readBy.add(userId);
            }
        } else
            throw new NotAuthorizedException();

    }

    @Before("args(userId, secretId,targetUserId) && execution(public * unshareSecret(..)))")
    public void validateUnShareSecret(String userId, UUID secretId, String targetUserId) {
        if (!secrets.containsKey(secretId) || !secrets.get(secretId).createdBy.equals(userId) || (!secrets.get(secretId).authorisedUsers.contains(targetUserId) && !secrets.get(secretId).sharedWith.contains(targetUserId)))
            throw new NotAuthorizedException();
        else {
            Secret secret = secrets.get(secretId);
            secret.authorisedUsers.remove(targetUserId);
            secret.sharedWith.remove(targetUserId);
        }
    }
}
