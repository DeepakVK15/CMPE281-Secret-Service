package edu.sjsu.cmpe275.aop.aspect;

import edu.sjsu.cmpe275.aop.NotAuthorizedException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Aspect
@Order(3)
public class AccessControlAspect {

    /***
     * Checks if the users have appropriate access to share/read/un-share secrets
     */

    public static String userID;
    public static UUID secretID;
    public static String targetUserID;

    public static final Map<UUID, StatsAspect.Secret> secrets = StatsAspect.secrets;
    public static final Map<String, StatsAspect.User> users = StatsAspect.users;

    @Before("args(userId, secretId, targetUserId) && execution(public * shareSecret(..)))")
    public void validateShareSecret(String userId, UUID secretId, String targetUserId) {
        Set<String> authorisedUsers = secrets.get(secretId).getAuthorisedUsers();
        if (!secrets.containsKey(secretId) || !authorisedUsers.contains(userId))
            throw new NotAuthorizedException();
        else {
            userID = userId;
            secretID = secretId;
            targetUserID = targetUserId;
        }
    }

    @Before("args(userId, secretId) && execution(public * readSecret(..)))")
    public void validateReadSecret(String userId, UUID secretId) {
        if (!secrets.containsKey(secretId))
            throw new NotAuthorizedException();

        StatsAspect.Secret secret = StatsAspect.secrets.get(secretId);
        if (secret.getAuthorisedUsers().contains(userId) || secret.getSharedWith().contains(userId)) {
            if (!userId.equals(secret.getCreatedBy())) {
                userID = userId;
                secretID = secretId;
            }
        } else
            throw new NotAuthorizedException();

    }

    @Before("args(userId, secretId,targetUserId) && execution(public * unshareSecret(..)))")
    public void validateUnshareSecret(String userId, UUID secretId, String targetUserId) {
        if (!secrets.containsKey(secretId) || !secrets.get(secretId).getCreatedBy().equals(userId) || (!secrets.get(secretId).getAuthorisedUsers().contains(targetUserId) && !secrets.get(secretId).getSharedWith().contains(targetUserId)))
            throw new NotAuthorizedException();
        else {
            secretID = secretId;
            targetUserID = targetUserId;
        }
    }
}
