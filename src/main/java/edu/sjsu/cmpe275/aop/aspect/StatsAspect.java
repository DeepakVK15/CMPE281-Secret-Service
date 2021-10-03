package edu.sjsu.cmpe275.aop.aspect;

import edu.sjsu.cmpe275.aop.SecretStatsImpl;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import java.util.*;

@Aspect
@Order(4)
public class StatsAspect {
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

		public Secret(String createdBy, String content) {
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

		public Set<String> getAuthorisedUsers(){
			return  this.authorisedUsers;
		}

		public Set<String> getSharedWith(){
			return  this.sharedWith;
		}

		public String getCreatedBy(){
			return this.createdBy;
		}

	}

	public  class User {
		private final Map<String, Set<UUID>> trustWorthyScore;
		private final Map<String,Set<UUID>> secretKeeperScore;

		public User() {
			trustWorthyScore = new HashMap<>();
			secretKeeperScore = new HashMap<>();
		}

		public int getTrustWorthyScore() {
			int score = 0;
			for(String user:trustWorthyScore.keySet())
				score+=trustWorthyScore.get(user).size();

			return score;
		}

		public int getSecretKeeperScore() {
			int score= 0;
			for(String user:secretKeeperScore.keySet())
				score+=secretKeeperScore.get(user).size();

			return score;
		}

	}

	public static final Map<UUID, StatsAspect.Secret> secrets = new HashMap<>();
	public static final Map<String, StatsAspect.User> users = new HashMap<>();

	@AfterReturning(pointcut = "execution(public * createSecret(..))", returning = "id")
	public void storeSecret(UUID id) {
		StatsAspect.Secret secret = new StatsAspect.Secret(ValidationAspect.getCreatedBy(), ValidationAspect.getContent());
		secrets.put(id, secret);

		if(secret.getContent().length() > SecretStatsImpl.lengthOfLongestSecret)
			SecretStatsImpl.lengthOfLongestSecret = secret.getContent().length();

		if (!users.containsKey(secret.createdBy))
			users.put(secret.createdBy, new StatsAspect.User());
	}

	@AfterReturning(pointcut = "execution(public * shareSecret(..))")
	public void afterShareSecret(){
		String userId = AccessControlAspect.userID;
		UUID secretId = AccessControlAspect.secretID;
		String targetUserId = AccessControlAspect.targetUserID;

		if (!users.containsKey(targetUserId)) {
			StatsAspect.User user = new StatsAspect.User();
			users.put(targetUserId, user);
		}

		if (!userId.equals(secrets.get(secretId).createdBy)) {
			Map<String,Set<UUID>> keeperScore = users.get(userId).secretKeeperScore;
			if(!keeperScore.containsKey(targetUserId))
				keeperScore.put(targetUserId,new HashSet<>());

			Set<UUID> temp = keeperScore.get(targetUserId);

			temp.add(secretId);

		}

		if(secrets.get(secretId).readBy.contains(targetUserId))
			secrets.get(secretId).authorisedUsers.add(targetUserId);
		else
			secrets.get(secretId).sharedWith.add(targetUserId);

		if(!targetUserId.equals(secrets.get(secretId).createdBy)) {
			Map<String, Set<UUID>> userScore = users.get(targetUserId).trustWorthyScore;

			if (!userScore.containsKey(userId))
				userScore.put(userId, new HashSet<>());

			Set<UUID> temp = userScore.get(userId);
			temp.add(secretId);
		}

		AccessControlAspect.userID = null;
		AccessControlAspect.targetUserID = null;
		AccessControlAspect.secretID =null;
	}

	@AfterReturning(pointcut = "execution(public * readSecret(..))" , returning = "secret")
	public void afterSecretRead(String secret) {
		UUID secretId = AccessControlAspect.secretID;
		String userId = AccessControlAspect.userID;
		if(secret!=null){
			secrets.get(secretId).authorisedUsers.add(userId);
			secrets.get(secretId).sharedWith.remove(userId);
			secrets.get(secretId).readBy.add(userId);
		}
		AccessControlAspect.secretID = null;
		AccessControlAspect.userID = null;
	}

	@AfterReturning(pointcut = "execution(public * unshareSecret(..))")
	public void afterUnshareSecret(){
		StatsAspect.Secret secret = secrets.get(AccessControlAspect.secretID);
		secret.authorisedUsers.remove(AccessControlAspect.targetUserID);
		secret.sharedWith.remove(AccessControlAspect.targetUserID);

		AccessControlAspect.secretID = null;
		AccessControlAspect.targetUserID = null;
	}
}
