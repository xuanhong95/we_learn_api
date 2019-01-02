package com.we_learn.dao;

import org.json.simple.JSONObject;

public interface ArticleTopicDao {
	JSONObject insert(String param, String user_id);
	JSONObject update(String param, String user_id);
	JSONObject delete(String param, String user_id);
	JSONObject remove(String param, String user_id);
	JSONObject getTopicByPage(String param);
	JSONObject getTopicById(String user_id);
	JSONObject getAllListArticleTopic();
}
