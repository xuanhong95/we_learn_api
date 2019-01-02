package com.we_learn.dao;

import org.json.simple.JSONObject;

public interface CreateTestDao {
	JSONObject insert(String param, int user_id);
	public JSONObject getTestByPage(String test_id);
	JSONObject getTestById(String param);
}
