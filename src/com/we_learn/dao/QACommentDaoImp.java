package com.we_learn.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import com.we_learn.common.MainUtility;

public class QACommentDaoImp implements QACommentDao{
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
		JSONObject data = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String content = jsonParams.get("content").toString();
		String qa_id = jsonParams.get("qa_id").toString();
		String query = "INSERT INTO `qa_comment`(`qa_comment_content`,`qa_id`, `created_by`) VALUES (?,?,?)";
		//insert
		try {
			Object[] objects = new Object[] {content,qa_id, user_id};
			int row = this.jdbcTemplate.update(query, objects);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		result.put("success", true);
		result.put("msg", "Comment create success");
		return result;
	}

	@Override
	public JSONObject update(String param, String user_id) {
		JSONObject data = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String content = jsonParams.get("content").toString();
		String qa_comment_id = jsonParams.get("qa_comment_id").toString();
		String query = "UPDATE `qa_comment` SET `qa_comment_content`=?,`modify_date`=?,`modify_by`=? WHERE `qa_comment_id` = ?";
		//insert
		try {
			String dateTimeNow = mainUtil.getDateFormat("yyyy-MM-dd HH:mm:ss", new Date());
			Object[] objects = new Object[] {content,dateTimeNow, user_id,qa_comment_id};
			int row = this.jdbcTemplate.update(query, objects);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			data.put("success", false);
			data.put("msg", e.getMessage());
			return data;
		}
		data.put("success", true);
		data.put("msg", "Comment update success");
		return data;
	}

	@Override
	public JSONObject listCommentByPage(String param) {
		// TODO Auto-generated method stub
		JSONObject data = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String qa_id = jsonParams.get("qa_id").toString();
		StringBuilder queryForComment = new StringBuilder();
		queryForComment.append("SELECT t1.`qa_comment_id` AS `comment_id`, t1.`qa_comment_content` AS `comment_content`,"
				+ "IF(t1.`created_date` IS NULL,null, DATE_FORMAT(t1.`created_date`, '%d-%m-%Y %H:%i:%s')) AS `created_date`, "
				+ "t2.`user_id`, t2.`user_login` FROM `qa_comment` t1 LEFT JOIN `crm_user` t2 ON t2.`user_id` = t1.`created_by` "
				+ "WHERE `qa_id` = ? AND t2.`deleted` <> 1 ORDER BY t1.`created_date` DESC");
		String queryForTotal = "SELECT COUNT(1) FROM `qa_comment` WHERE `qa_id` = ?";
		mainUtil.getLimitOffset(queryForComment, jsonParams);
		try {
			int totalRow = this.jdbcTemplate.queryForObject(queryForTotal, Integer.class, new Object[] {qa_id});
			List<Map<String, Object>> listComment = this.jdbcTemplate.queryForList(queryForComment.toString(), new Object[] {qa_id});
			JSONObject results = new JSONObject();
			results.put("results", listComment);
			results.put("total", totalRow);
			data.put("data", results);
			data.put("success", true);
		} catch (Exception e) {
			data.put("success", false);
			data.put("err", e.getMessage());
			data.put("msg", "Lấy danh sách comment thất bại");
		}
		return data;
	}

}
