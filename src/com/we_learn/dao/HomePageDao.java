package com.we_learn.dao;

import org.json.simple.JSONObject;

public interface HomePageDao {
	JSONObject getNewestByPage(String param);
	JSONObject getNewestGrammarByPage(String param);
}
