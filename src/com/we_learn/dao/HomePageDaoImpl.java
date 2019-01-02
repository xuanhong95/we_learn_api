package com.we_learn.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import com.we_learn.dao.HomePageDaoImpl;
import com.we_learn.common.MainUtility;

public class HomePageDaoImpl implements HomePageDao {
	private JdbcTemplate jdbcTemplate;
	private Logger logger = Logger.getLogger(HomePageDaoImpl.class);

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public JSONObject getNewestByPage(String param) {
		// TODO Auto-generated method stub
		JSONObject data = new JSONObject();
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		StringBuilder builder = new StringBuilder();
		StringBuilder builderGetTotal = new StringBuilder();

		builder.append("SELECT atc.atc_id, atc.atc_title, 'https://cdn-images-1.medium.com/max/1200/1*EPHVYygppZ2py-HQ57CSqA.jpeg' AS img_url, "
				+ "atc.deleted, user.full_name AS created_by_name, atc.atc_content, "
				+ "IF(atc.created_date IS NULL,null, DATE_FORMAT(atc.created_date, '%d-%m-%Y')) AS created_date "
				+ "FROM article_topic_content AS atc "
				+ "LEFT JOIN crm_user AS user ON atc.created_by = user.user_id WHERE atc.deleted <> 1 AND atc.is_paid = 0 "
				+ "ORDER BY atc.created_date DESC");
		builderGetTotal.append("SELECT COUNT(1) FROM article_topic_content AS atc "
				+ "LEFT JOIN crm_user AS user ON atc.created_by = user.user_id WHERE atc.deleted <> 1 AND atc.is_paid = 0");
		// lấy các biến từ table (limit, offset)
		mainUtil.getLimitOffset(builder, jsonParams);
		try {
			int totalRow = this.jdbcTemplate.queryForObject(builderGetTotal.toString(), Integer.class);
			List<Map<String, Object>> listNewest = this.jdbcTemplate.queryForList(builder.toString());
			JSONObject results = new JSONObject();
			results.put("results", listNewest);
			results.put("total", totalRow);
			data.put("data", results);
			data.put("success", true);
		} catch (Exception e) {
			data.put("success", false);
			data.put("err", e.getMessage());
			data.put("msg", "Lấy danh sách tin mới thất bại");
		}
		return data;
	}

	@Override
	public JSONObject getNewestGrammarByPage(String param) {
		// TODO Auto-generated method stub
		JSONObject data = new JSONObject();
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		StringBuilder builder = new StringBuilder();
		StringBuilder builderGetTotal = new StringBuilder();

		builder.append(
				"SELECT article.article_id, article.article_title, 'https://cdn-images-1.medium.com/max/1200/1*EPHVYygppZ2py-HQ57CSqA.jpeg' AS img_url, "
						+ "article.deleted, user.full_name AS created_by_name, article.article_content, "
						+ "IF(article.created_date IS NULL,null, DATE_FORMAT(article.created_date, '%d-%m-%Y')) AS created_date FROM article "
						+ "LEFT JOIN crm_user AS user ON article.created_by = user.user_id WHERE article.type_id = 2 AND article.deleted <> 1 "
						+ "ORDER BY article.created_date DESC");
		builderGetTotal.append("SELECT COUNT(1) FROM article "
				+ "LEFT JOIN crm_user AS user ON article.created_by = user.user_id WHERE article.type_id = 2 AND article.deleted <> 1");
		// lấy các biến từ table (limit, offset)
		mainUtil.getLimitOffset(builder, jsonParams);
		try {
			int totalRow = this.jdbcTemplate.queryForObject(builderGetTotal.toString(), Integer.class);
			List<Map<String, Object>> listNewest = this.jdbcTemplate.queryForList(builder.toString());
			JSONObject results = new JSONObject();
			results.put("results", listNewest);
			results.put("total", totalRow);
			data.put("data", results);
			data.put("success", true);
		} catch (Exception e) {
			data.put("success", false);
			data.put("err", e.getMessage());
			data.put("msg", "Lấy danh sách bài ngữ pháp mới thất bại");
		}
		return data;
	}
}
