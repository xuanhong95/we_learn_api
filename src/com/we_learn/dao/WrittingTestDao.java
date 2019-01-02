package com.we_learn.dao;

import org.json.simple.JSONObject;

public interface WrittingTestDao {
	JSONObject insert(String param, String user_id);
	JSONObject getById(String wt_id);
	JSONObject getByPage(String param);
	JSONObject getAllByUserId(String user_id);
}
