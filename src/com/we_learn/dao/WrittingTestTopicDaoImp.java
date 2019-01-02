package com.we_learn.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import com.we_learn.common.MainUtility;

public class WrittingTestTopicDaoImp implements WrittingTestTopicDao {
	private JdbcTemplate jdbcTemplate;
	private Logger logger = Logger.getLogger(QADaoImpl.class);

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
		String wt_id = jsonParams.get("wt_id").toString();
		String content = jsonParams.get("wtt_content").toString();
		String query = "INSERT INTO `writing_test_topic`(`wt_id`, `wtt_content`, `is_premium`, `created_by`) "
				+ "VALUES (?, ?, IF((SELECT `group_id` FROM `crm_user` WHERE `user_id` = ?) <> 3, 0,1), ?)";
		try {
			Object[] objects = new Object[] { wt_id, content, user_id, user_id };
			int row = this.jdbcTemplate.update(query, objects);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		result.put("success", true);
		return result;
	}

	@Override
	public JSONObject getTopicByPage(String param) {
		// TODO Auto-generated method stub
		JSONObject data = new JSONObject();
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		StringBuilder builder = new StringBuilder();
		StringBuilder builderGetTotal = new StringBuilder();

		builder.append("SELECT wtt.`wtt_id`,wtt.`wtt_content`, wtt.`is_premium`, "
				+ "DATE_FORMAT(wtt.created_date, '%d-%m-%Y') AS created_date, crm_user.full_name, "
				+ "writing_test.wt_title, "
				+ "(SELECT COUNT(wtc.wtc_id) FROM writing_test_comment wtc WHERE wtc.wtt_id = wtt.wtt_id) AS comments "
				+ "FROM `writing_test_topic` wtt " + "LEFT JOIN crm_user ON (wtt.created_by = crm_user.user_id) "
				+ "LEFT JOIN writing_test ON (writing_test.wt_id = wtt.wt_id) " + "WHERE 1 = 1");
		builderGetTotal.append("SELECT COUNT(1) FROM `writing_test_topic` wtt "
				+ "LEFT JOIN crm_user ON (crm_user.user_id = wtt.created_by) "
				+ "LEFT JOIN writing_test ON (writing_test.wt_id = wtt.wt_id) " + "WHERE 1 = 1");
		// filter header
		if (jsonParams.get("status") == null || Integer.parseInt(jsonParams.get("status").toString()) == -1) {
			builder.append(" AND wtt.deleted <> 1");
			builderGetTotal.append(" AND wtt.deleted <> 1");
		} else if (Integer.parseInt(jsonParams.get("status").toString()) == -2) {// thÃ¹ng rÃ¡c
			builder.append(" AND wtt.deleted = 1");
			builderGetTotal.append(" AND deleted = 1");
		}
		// if (Integer.parseInt(jsonParams.get("type_id").toString()) > -1) {
		// builder.append(" AND article.type_id=" + jsonParams.get("type_id"));
		// builderGetTotal.append(" AND article.type_id=" + jsonParams.get("type_id"));
		// }
		if (jsonParams.get("full_name") != null && !"".equals(jsonParams.get("full_name").toString())) {
			builder.append(" AND crm_user.full_name LIKE N'%" + jsonParams.get("full_name").toString() + "%'");
			builderGetTotal.append(" AND crm_user.full_name LIKE N'%" + jsonParams.get("full_name").toString() + "%'");
		}
		// sortby
		if (jsonParams.get("sortField") != null && !"".equals(jsonParams.get("sortField").toString())) {
			switch (jsonParams.get("sortField").toString()) {
			default:
				builder.append(" ORDER BY wtt.created_date DESC");
				break;
			}
			// if (jsonParams.get("sortOrder") != null &&
			// "descend".equals(jsonParams.get("sortOrder").toString())) {
			// builder.append(" DESC");
			// }
			// if (jsonParams.get("sortOrder") != null &&
			// "ascend".equals(jsonParams.get("sortOrder").toString())) {
			// builder.append(" ASC");
			// }
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
			data.put("msg", "Lấy danh sách bài làm thất bại");
		}
		return data;
	}

	@Override
	public JSONObject getById(String wtt_id) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();

		String queryForTopic = "SELECT wtt.wtt_id, wtt_content, "
				+ "DATE_FORMAT(wtt.created_date, '%d-%m-%Y %h:%i') AS created_date, "
				+ "wt.wt_title, wt.wt_content, crm_user.full_name " + "FROM writing_test_topic AS wtt "
				+ "LEFT JOIN writing_test AS wt ON wtt.wt_id = wt.wt_id "
				+ "LEFT JOIN crm_user ON crm_user.user_id = wtt.created_by " + "WHERE wtt.wtt_id = ?";

		String queryLstByUserComment = "SELECT wtc.wtc_id AS comment_id, wtc.wtc_content AS comment_content, "
				+ "wtc.is_admin, crm_user.user_login, wtc.created_by AS user_id, "
				+ "DATE_FORMAT(wtc.created_date, '%d-%m-%Y %h:%i') AS created_date "
				+ "FROM writing_test_comment AS wtc "
				+ "LEFT JOIN crm_user ON crm_user.user_id = wtc.created_by WHERE wtc.wtt_id = ? AND is_admin = ?";
		try {
			Map<String, Object> topicItem = this.jdbcTemplate.queryForMap(queryForTopic, new Object[] { wtt_id });
			List<Map<String, Object>> lstCommentByUser = this.jdbcTemplate.queryForList(queryLstByUserComment,
					new Object[] { wtt_id, 0 });
			List<Map<String, Object>> lstCommentByManager = this.jdbcTemplate.queryForList(queryLstByUserComment,
					new Object[] { wtt_id, 1 });
			// test.put("lstTopic", lstTopic);
			result.put("topicItem", topicItem);
			result.put("lstCommentByUser", lstCommentByUser);
			result.put("lstCommentByManager", lstCommentByManager);
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("err", e.getMessage());
			result.put("msg", "Lấy bài làm thất bại");
		}
		return result;
	}
	
	@Override
	public JSONObject insertComment(String param, String user_id, String groupCode) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String content = jsonParams.get("content").toString();
		String wtt_id = jsonParams.get("wtt_id").toString();
		String query = "INSERT INTO writing_test_comment(wtc_content,wtt_id, created_by, is_admin) VALUES (?,?,?,?)";
		String sqlUpdateManagerChecked = "UPDATE writing_test_topic SET is_premium = 0 WHERE wtt_id = " + wtt_id;
		//insert
		try {
			int is_admin = 0;
			if(!groupCode.equals("USER") && !groupCode.equals("USER_PRE")) {
				is_admin = 1;
				this.jdbcTemplate.update(sqlUpdateManagerChecked);
			}
			Object[] objects = new Object[] {content,wtt_id, user_id, is_admin};
			int row = this.jdbcTemplate.update(query, objects);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		result.put("success", true);
		return result;
	}

	@Override
	public JSONObject updateComment(String param, String user_id) {
		JSONObject data = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String content = jsonParams.get("content").toString();
		String wtc_id = jsonParams.get("wtc_id").toString();
		String query = "UPDATE `writing_test_comment` SET `wtc_content`=?,`modify_date`=?,`modify_by`=? WHERE `wtc_id` = ?";
		//insert
		try {
			String dateTimeNow = mainUtil.getDateFormat("yyyy-MM-dd HH:mm:ss", new Date());
			Object[] objects = new Object[] {content,dateTimeNow, user_id,wtc_id};
			int row = this.jdbcTemplate.update(query, objects);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			data.put("success", false);
			data.put("msg", e.getMessage());
			return data;
		}
		data.put("success", true);
		return data;
	}
}
