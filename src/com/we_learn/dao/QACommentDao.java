package com.we_learn.dao;

import org.json.simple.JSONObject;

public interface QACommentDao {
	JSONObject insert(String param, String user_id);
	JSONObject update(String param, String user_id);
	JSONObject listCommentByPage(String param);
}
