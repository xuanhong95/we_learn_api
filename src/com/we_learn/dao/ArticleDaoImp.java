package com.we_learn.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import com.we_learn.common.MainUtility;

public class ArticleDaoImp implements ArticleDao{
	private JdbcTemplate jdbcTemplate;
	private Logger logger = Logger.getLogger(CreateTestDaoImpl.class);

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public JSONObject insert(String param, String user_id) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String title = jsonParams.get("article_title").toString();
		String type_id = jsonParams.get("type_id").toString();
		
		String query = "INSERT INTO `article`(`article_title`, `type_id`, `created_by`) VALUES (?,?,?)";
		try {
			int row = this.jdbcTemplate.update(query, new Object[] {title, type_id, user_id});
		} catch (Exception e) {
			// TODO: handle exception
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		result.put("success", true);
		result.put("msg", "Insert thanh cong");
		return result;
	}

	@Override
	public JSONObject update(String param, String user_id) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String title = jsonParams.get("article_title").toString();
		String type_id = jsonParams.get("type_id").toString();
		String article_id = jsonParams.get("article_id").toString();
		String query = "UPDATE `article` SET `article_title`=?,`type_id`=?,`modify_date`=?,`modify_by`=? WHERE `article_id` = ?";
		try {
			String dateTimeNow = mainUtil.getDateFormat("yyyy-MM-dd HH:mm:ss", new Date());
			int row = this.jdbcTemplate.update(query, new Object[] {title, type_id,dateTimeNow, user_id, article_id});
		} catch (Exception e) {
			// TODO: handle exception
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		result.put("success", true);
		result.put("msg", "Update thanh cong");
		return result;
	}

	@Override
	public JSONObject delete(String param, String user_id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject remove(String param, String user_id) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public JSONObject getListArticleByType(String param) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String article_type_id = jsonParams.get("type_id").toString();
		String queryForListArticle = "SELECT `article_id`, `article_title`, `article_content` FROM `article` WHERE `type_id` = ? AND `deleted` <> 1";
		try {
			List<Map<String, Object>> listArticle = this.jdbcTemplate.queryForList(queryForListArticle, new Object[] {article_type_id});
			for (Map<String, Object> article : listArticle) {
				String queryForListTopic = "SELECT `at_id`, `at_title`, `at_content` FROM `article_topic` WHERE `article_id` = ? AND `deleted` <> 1";
				List<Map<String, Object>> listTopic = this.jdbcTemplate.queryForList(queryForListTopic, new Object[] {article.get("article_id")});
				for (Map<String, Object> topic : listTopic) {
					String queryForListContent = "SELECT `atc_id`, `atc_title`, `atc_content` FROM `article_topic_content` WHERE `at_id` = ? AND `deleted` <> 1";
					List<Map<String, Object>> listContent = this.jdbcTemplate.queryForList(queryForListContent, new Object[] {topic.get("at_id")});
					topic.put("listContent", listContent);
				}
				article.put("listTopic", listTopic);
			}
			result.put("success", true);
			result.put("data", listArticle);
			return result;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		
	}

	@Override
	public JSONObject getArticleByPage(String param) {
		JSONObject data = new JSONObject();
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		StringBuilder builder = new StringBuilder();
		StringBuilder builderGetTotal = new StringBuilder();

		builder.append("SELECT `article_id`, `article_content`,  article_title, "
				+ "DATE_FORMAT(article.created_date, '%d-%m-%Y') AS created_date, "
				+ "user.full_name, article_type.article_type_name "
				+ "FROM `article` "
				+ "LEFT JOIN article_type ON article.type_id = article_type.article_type_id "
				+ "LEFT JOIN crm_user AS user ON article.created_by = user.user_id "
				+ "WHERE 1=1");
		builderGetTotal.append("SELECT COUNT(1) FROM article "
				+ "LEFT JOIN article_type ON article.type_id = article_type.article_type_id "
				+ "LEFT JOIN crm_user AS user ON article.created_by = user.user_id WHERE 1=1");
		// filter header
		if (jsonParams.get("status") == null || Integer.parseInt(jsonParams.get("status").toString()) == -1) {
			builder.append(" AND article.deleted <> 1");
			builderGetTotal.append(" AND article.deleted <> 1");
		} else if (Integer.parseInt(jsonParams.get("status").toString()) == -2) {// thùng rác
			builder.append(" AND article.deleted = 1");
			builderGetTotal.append(" AND article.deleted = 1");
		}
		if (Integer.parseInt(jsonParams.get("type_id").toString()) > -1) {
			builder.append(" AND article.type_id=" + jsonParams.get("type_id"));
			builderGetTotal.append(" AND article.type_id=" + jsonParams.get("type_id"));
		}
		if (jsonParams.get("article_title") != null && !"".equals(jsonParams.get("article_title").toString())) {
			builder.append(" AND article_title LIKE N'%" + jsonParams.get("article_title").toString() + "%'");
			builderGetTotal
					.append(" AND article_title LIKE N'%" + jsonParams.get("article_title").toString() + "%'");
		}
		// sortby
		if (jsonParams.get("sortField") != null && !"".equals(jsonParams.get("sortField").toString())) {
			switch (jsonParams.get("sortField").toString()) {
			default:
				builder.append(" ORDER BY created_date DESC");
				break;
			}
//			if (jsonParams.get("sortOrder") != null && "descend".equals(jsonParams.get("sortOrder").toString())) {
//				builder.append(" DESC");
//			}
//			if (jsonParams.get("sortOrder") != null && "ascend".equals(jsonParams.get("sortOrder").toString())) {
//				builder.append(" ASC");
//			}
		}
		mainUtil.getLimitOffset(builder, jsonParams);
		try {
			int totalRow = this.jdbcTemplate.queryForObject(builderGetTotal.toString(), Integer.class);
			List<Map<String, Object>> listArticle = this.jdbcTemplate.queryForList(builder.toString());
			JSONObject results = new JSONObject();
			results.put("results", listArticle);
			results.put("total", totalRow);
			data.put("data", results);
			data.put("success", true);
		} catch (Exception e) {
			data.put("success", false);
			data.put("err", e.getMessage());
			data.put("msg", "Lấy danh mục thất bại");
		}
		return data;
	}

	@Override
	public JSONObject getArticleById(String article_id) {
		JSONObject result = new JSONObject();
		String query = "SELECT `article_title`,`type_id` FROM `article` WHERE `article_id` = " + article_id;
		try {
			Map<String, Object> articleObject = this.jdbcTemplate.queryForMap(query);
			result.put("success", true);
			result.put("data", articleObject);
		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@Override
	public JSONObject getAllListArticle() {
		JSONObject result = new JSONObject();
		String query = "SELECT `article_title`,`article_id` FROM `article` WHERE deleted <> 1";
		try {
			List<Map<String, Object>> lstArticle = this.jdbcTemplate.queryForList(query);
			result.put("success", true);
			result.put("data", lstArticle);
		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", e.getMessage());
		}
		return result;
	}
}
