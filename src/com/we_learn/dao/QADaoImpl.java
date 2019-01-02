package com.we_learn.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import com.we_learn.dao.QADaoImpl;
import com.we_learn.common.MainUtility;

public class QADaoImpl implements QADao{
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
		String title = jsonParams.get("qa_title").toString();
		String content = jsonParams.get("qa_content").toString();
		
		String query = "INSERT INTO `question_answer`(`qa_title`, `qa_content`, `created_by`) VALUE (?,?,?)";
		//insert
		try {
			Object[] objects = new Object[] {title, content, user_id};
			int row = this.jdbcTemplate.update(query, objects);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		result.put("success", true);
		result.put("msg", "Article create success");
		return result;
	}

	@Override
	public JSONObject update(String param, String user_id) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String title = jsonParams.get("qa_title").toString();
		String content = jsonParams.get("qa_content").toString();
		String qa_id = jsonParams.get("qa_id").toString();
		String query = "UPDATE `question_answer` SET `qa_title`=?, `qa_content` =?, `modify_date` =?, `modify_by` = ? WHERE `qa_id` = ?";
		//insert
		try {
			String dateTimeNow = mainUtil.getDateFormat("yyyy-MM-dd HH:mm:ss", new Date());
			Object[] objects = new Object[] {title, content, dateTimeNow, user_id, qa_id};
			int row = this.jdbcTemplate.update(query, objects);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		result.put("success", true);
		result.put("msg", "Article update success");
		return result;
	}

	@Override
	public JSONObject getQAByPage(String param) {
		// TODO Auto-generated method stub
		JSONObject data = new JSONObject();
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		StringBuilder builder = new StringBuilder();
		StringBuilder builderGetTotal = new StringBuilder();
		
		builder.append(
				"SELECT qa.qa_id, qa.qa_title, (SELECT COUNT(qa_id) FROM qa_comment WHERE qa_comment.qa_id = qa.qa_id) AS comment_number, "
						+ "qa.deleted, user.full_name, "
						+ "IF(qa.created_date IS NULL,null, DATE_FORMAT(qa.created_date, '%d-%m-%Y')) AS created_date FROM question_answer AS qa "
						+ "LEFT JOIN crm_user AS user ON qa.created_by = user.user_id WHERE 1=1 ");
		builderGetTotal.append("SELECT COUNT(1) FROM question_answer AS qa "
				+ "LEFT JOIN crm_user AS user ON qa.created_by = user.user_id ");
		// filter header
		if (jsonParams.get("status") == null || Integer.parseInt(jsonParams.get("status").toString()) == -1) {
			builder.append(" AND qa.deleted <> 1");
			builderGetTotal.append(" AND qa.deleted <> 1");
		} else if (Integer.parseInt(jsonParams.get("status").toString()) == -2) {// thùng rác
			builder.append(" AND qa.deleted = 1");
			builderGetTotal.append(" AND qa.deleted = 1");
		}
		if (jsonParams.get("qa_title") != null && !"".equals(jsonParams.get("qa_title").toString())) {
			builder.append(" AND qa.qa_title LIKE N'%" + jsonParams.get("qa_title").toString()
					+ "%'");
			builderGetTotal.append(" AND qa.qa_title LIKE N'%"
					+ jsonParams.get("qa_title").toString() + "%'");
		}
		// sortby
		if (jsonParams.get("sortField") != null && !"".equals(jsonParams.get("sortField").toString())) {
			switch (jsonParams.get("sortField").toString()) {
			default:
				builder.append(" ORDER BY qa.created_date DESC");
				break;
			}
			// sortOrder chỉ là descend và ascend hoặc rỗng
			if (jsonParams.get("sortOrder") != null && "descend".equals(jsonParams.get("sortOrder").toString())) {
				builder.append(" DESC");
			}
			if (jsonParams.get("sortOrder") != null && "ascend".equals(jsonParams.get("sortOrder").toString())) {
				builder.append(" ASC");
			}
		}
		// lấy các biến từ table (limit, offset)
		mainUtil.getLimitOffset(builder, jsonParams);
		try {
			int totalRow = this.jdbcTemplate.queryForObject(builderGetTotal.toString(), Integer.class);
			List<Map<String, Object>> listQA = this.jdbcTemplate.queryForList(builder.toString());
			JSONObject results = new JSONObject();
			results.put("results", listQA);
			results.put("total", totalRow);
			data.put("data", results);
			data.put("success", true);
		} catch (Exception e) {
			data.put("success", false);
			data.put("err", e.getMessage());
			data.put("msg", "Lấy danh sách bài viết thất bại");
		}
		return data;
	}
	
	@Override
	public JSONObject delete(String qa, int user_id) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(qa);
		String query = "DELETE FROM question_answer WHERE qa_id IN ("
				+ jsonParams.get("qa_id") + ")";
		try {
			int row = this.jdbcTemplate.update(query);
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
//			result.put("msg", "Xóa b);
			 result.put("msg", "Xóa bài viết thất bại");
		}
		return result;
	}

	@Override
	public JSONObject remove(String qa, int user_id) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(qa);
		// Sẽ phải check bên place địa điểm đã sử dụng ở bản ghi nào chưa
		try {
			String query = "UPDATE question_answer AS qa SET qa.deleted = 1, qa.modify_date = ?, "
					+ "qa.modify_by = ? WHERE qa.qa_id = ?";
			int row = this.jdbcTemplate.update(query,
					new Object[] { mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"), user_id,
							jsonParams.get("qa_id") });
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", e.getMessage());
			// result.put("msg", "Chuyển loại địa điểm vào thùng rác thất bại");
		}
		return result;
	}

	@Override
	public JSONObject restore(String qa, int user_id) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(qa);
		String sql = "UPDATE question_answer AS qa SET qa.deleted = 0, qa.modify_date = ?, qa.modify_by = ?"
				+ " WHERE qa.qa_id IN (" + jsonParams.get("qa_id") + ")";
		try {
			this.jdbcTemplate.update(sql,
					new Object[] { mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"), user_id });
			result.put("success", true);
		} catch (Exception e) {
			logger.info(e.getMessage());
			result.put("success", false);
			result.put("msg", e.getMessage());
			// result.put("msg", "Restore loại địa điểm thất bại");
		}
		return result;
	}
	//Hàm update qa by id
	@Override
	public JSONObject getQAById(String qa_id) {
		JSONObject result = new JSONObject();
		String query = "SELECT qa.qa_title, qa.qa_content "
				+ "FROM question_answer AS qa "
				+ "WHERE qa.qa_id = " + qa_id;
		try {
			Map<String, Object> qaObject = this.jdbcTemplate.queryForMap(query);

			result.put("success", true);
			result.put("data", qaObject);
		} catch (Exception e) {
			result.put("success", false);
			result.put("err", e.getMessage());
			result.put("msg", "Không lấy được thông tin bài viết. Kiểm tra lại");
		}
		return result;
	}
	//Hàm xem qa by id
	@Override
	public JSONObject viewQAById(String qa_id) {
		JSONObject result = new JSONObject();
		String query = "SELECT qa.qa_title, qa.qa_content, user.user_login "
				+ "FROM question_answer AS qa "
				+ "LEFT JOIN crm_user AS user ON qa.created_by = user.user_id "
				+ "WHERE qa.qa_id = " + qa_id;
		try {
			Map<String, Object> qaObject = this.jdbcTemplate.queryForMap(query);

			result.put("success", true);
			result.put("data", qaObject);
		} catch (Exception e) {
			result.put("success", false);
			result.put("err", e.getMessage());
			result.put("msg", "Không lấy được thông tin bài viết. Kiểm tra lại");
		}
		return result;
	}
}
