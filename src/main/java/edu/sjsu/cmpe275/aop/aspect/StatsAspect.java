package edu.sjsu.cmpe275.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.UUID;

@Aspect
@Order(4)
public class StatsAspect {
    /***
     * Following is a dummy implementation of this aspect.
     * You are expected to provide an actual implementation based on the requirements, including adding/removing advices as needed.
     */

//	Map<String, AccessControlAspect.User> users = AccessControlAspect.users;
//	Map<UUID, AccessControlAspect.Secret> secrets = AccessControlAspect.secrets;
//
//	public static String userId;
//	public static UUID secretId;
//	public static String targetUserId;

	@Around("execution(public * edu.sjsu.cmpe275.aop.SecretStats.*(..))")
	public  Object retryStatsService(ProceedingJoinPoint joinPoint) throws Throwable {
		Object result = null;
		int retry = 0;
		for(;;) {
			try {
				result = joinPoint.proceed();
				System.out.printf("Finished the execution of the method %s\n", joinPoint.getSignature().getName());
				return result;
			} catch (Exception e) {
				retry++;
				if (retry > 2) {
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

//	@AfterReturning(pointcut = "execution(public * readSecret(..))" , returning = "secret")
//	public void afterSecretRead(String secret) {
//		if(secret!=null){
//			secrets.get(secretId).authorisedUsers.add(userId);
//			secrets.get(secretId).sharedWith.remove(userId);
//			secrets.get(secretId).readBy.add(userId);
//		}
//		secretId = null;
//		userId = null;
//	}
//
//	@AfterReturning(pointcut = "execution(public * shareSecret(..))")
//	public void afterShareSecret(){
//		if (!userId.equals(secrets.get(secretId).createdBy)) {
//			users.get(userId).secretKeeperScore++;
//		}
//		if (!users.containsKey(targetUserId)) {
//			AccessControlAspect.User user = new AccessControlAspect.User(targetUserId);
//			users.put(targetUserId, user);
//		}
//		if(secrets.get(secretId).readBy.contains(targetUserId))
//			secrets.get(secretId).authorisedUsers.add(targetUserId);
//		else
//			secrets.get(secretId).sharedWith.add(targetUserId);
//
//		users.get(targetUserId).trustWorthyScore++;
//	}


}
