package com.we_learn.dao;

import org.json.simple.JSONObject;

public interface UserDao {
	public JSONObject getUserByPage(String param);
	public JSONObject activePremium(String param);
	public JSONObject updateUserPassword(String user, int user_id);
}
