package com.we_learn.dao;

import java.util.Map;

import org.json.simple.JSONObject;

public interface LoginDao {
	public JSONObject login(String params);

	public JSONObject logout(String token);

	public JSONObject signUp(String params, String rootUrl);

	public JSONObject activeAccount(String params);

	public JSONObject resendActiveCode(String params, String rootUrl);

}
