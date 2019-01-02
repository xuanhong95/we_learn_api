package com.we_learn.dao;

import org.json.simple.JSONObject;

public interface QADao {
	JSONObject insert(String param, String user_id);
	JSONObject update(String param, String user_id);
	public JSONObject delete(String qa, int user_id);
	public JSONObject remove(String qa, int user_id);
	public JSONObject restore(String qa, int user_id);
	public JSONObject getQAById(String qa_id);
	public JSONObject viewQAById(String qa_id);
	JSONObject getQAByPage(String param);
}
