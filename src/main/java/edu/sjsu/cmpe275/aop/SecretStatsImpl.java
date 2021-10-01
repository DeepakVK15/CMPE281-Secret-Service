package edu.sjsu.cmpe275.aop;

import edu.sjsu.cmpe275.aop.aspect.AccessControlAspect;

import java.util.Map;
import java.util.UUID;

public class SecretStatsImpl implements SecretStats {
    /***
     * This is a dummy implementation only.
     * You are expected to provide an actual implementation based on the requirements.
     */
    public static int lengthOfLongestSecret = 0;
	Map<String, AccessControlAspect.User> users = AccessControlAspect.users;
	Map<UUID, AccessControlAspect.Secret> secrets = AccessControlAspect.secrets;

	@Override
	public void resetStatsAndSystem() {
		// TODO Auto-generated method stub
		AccessControlAspect.users.clear();
		AccessControlAspect.secrets.clear();
		lengthOfLongestSecret = 0;
	}

	@Override
	public int getLengthOfLongestSecret() {
		// TODO Auto-generated method stub
		return lengthOfLongestSecret;
	}

	@Override
	public String getMostTrustedUser() {
		// TODO Auto-generated method stub
		String mostTrustedUser = null;
		int max = 0;
		for(String user:users.keySet()){
			int score = users.get(user).getTrustWorthyScore();
			if(score>max) {
				mostTrustedUser = user;
				max = users.get(user).getTrustWorthyScore();
			}
			else if(score==max && mostTrustedUser!=null) {
				mostTrustedUser = mostTrustedUser.compareTo(user)>0?user:mostTrustedUser;
			}
		}

		return mostTrustedUser;
	}

	@Override
	public String getWorstSecretKeeper() {
		// TODO Auto-generated method stub
		int maxScore = 0;
		String resultId = null;
		for(String name:users.keySet()){
			int keeperScore = users.get(name).getSecretKeeperScore();
			if(keeperScore!=0) {
				int temp = keeperScore / users.get(name).getTrustWorthyScore();
				if(temp>maxScore) {
					maxScore = temp;
					resultId = name;
				}
				else if(temp==maxScore){
					if(resultId!=null)
						resultId = resultId.compareTo(name)>0?name:resultId;
					else
						resultId=name;
				}
			}
		}
		return resultId;
	}

	@Override
	public UUID getBestKnownSecret() {
		// TODO Auto-generated method stub
		UUID bestSecret = null;
		int max = 0;
		for(UUID id:secrets.keySet()){
			int size = secrets.get(id).getReadBy().size();
			if(size>max) {
				max = size;
				bestSecret = id;
			}
			else if(size==max){
				if(bestSecret!=null) {
					int temp = secrets.get(bestSecret).getContent().compareToIgnoreCase(secrets.get(id).getContent());
					if (temp > 0)
						bestSecret = id;
				}
			}
		}

		return bestSecret;
	}
}
