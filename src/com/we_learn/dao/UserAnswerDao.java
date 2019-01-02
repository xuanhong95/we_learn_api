package com.we_learn.dao;

import org.json.simple.JSONObject;

public interface UserAnswerDao {
	JSONObject userAnswer(String param, String user_id);
}
