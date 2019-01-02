package com.we_learn.dao;

import java.util.Map;

import org.json.simple.JSONObject;

public interface DocumentDao {
	JSONObject insert(Map<String, Object> param, String user_id);
	JSONObject getDocByPage(String params, String user_id, String group_code, String addingUrl);
}
