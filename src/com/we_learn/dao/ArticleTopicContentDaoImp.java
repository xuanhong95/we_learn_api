package com.we_learn.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import com.we_learn.common.MainUtility;

public class ArticleTopicContentDaoImp implements ArticleTopicContentDao {
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
		String title = jsonParams.get("atc_title").toString();
		String content = jsonParams.get("atc_content").toString();
		String type = jsonParams.get("at_id").toString();

		String query = "INSERT INTO `article_topic_content`(`atc_title`, `atc_content`, `created_by`, `at_id`) VALUE (?,?,?,?)";
		// insert
		try {
			Object[] objects = new Object[] { title, content, user_id, type };
			int row = this.jdbcTemplate.update(query, objects);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		result.put("success", true);
		result.put("msg", "Thêm mới bài viết thành công");
		return result;
	}

	@Override
	public JSONObject update(String param, String user_id) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String title = jsonParams.get("atc_title").toString();
		String content = jsonParams.get("atc_content").toString();
		String type = jsonParams.get("at_id").toString();
		String atc_id = jsonParams.get("atc_id").toString();
		String query = "UPDATE `article_topic_content` SET `atc_title`=?, `atc_content` =?,"
				+ "`at_id` = ?, `modify_date` =?, `modify_by` = ? WHERE `atc_id` = ?";
		// insert
		try {
			String dateTimeNow = mainUtil.getDateFormat("yyyy-MM-dd HH:mm:ss", new Date());
			Object[] objects = new Object[] { title, content, type, dateTimeNow, user_id, atc_id };
			int row = this.jdbcTemplate.update(query, objects);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		result.put("success", true);
		result.put("msg", "Cập nhật bài viết thành công");
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

		builder.append("SELECT atc.atc_id, atc.atc_title, topic.at_title, "
				+ "atc.deleted, user.full_name, "
				+ "IF(atc.created_date IS NULL,null, DATE_FORMAT(atc.created_date, '%d-%m-%Y')) AS created_date "
				+ "FROM article_topic_content AS atc "
				+ "LEFT JOIN article_topic AS topic ON atc.at_id = topic.at_id "
				+ "LEFT JOIN crm_user AS user ON atc.created_by = user.user_id WHERE 1=1 ");
		builderGetTotal.append("SELECT COUNT(1) FROM article_topic_content AS atc "
				+ "LEFT JOIN article_topic AS topic ON atc.at_id = topic.at_id "
				+ "LEFT JOIN crm_user AS user ON atc.created_by = user.user_id WHERE 1=1 ");
		// filter header
		if (jsonParams.get("status") == null || Integer.parseInt(jsonParams.get("status").toString()) == -1) {
			builder.append(" AND atc.deleted <> 1");
			builderGetTotal.append(" AND atc.deleted <> 1");
		} else if (Integer.parseInt(jsonParams.get("status").toString()) == -2) {// thùng rác
			builder.append(" AND atc.deleted = 1");
			builderGetTotal.append(" AND atc.deleted = 1");
		}
		if (Integer.parseInt(jsonParams.get("at_id").toString()) > -1) {
			builder.append(" AND atc.at_id=" + jsonParams.get("at_id"));
			builderGetTotal.append(" AND atc.at_id=" + jsonParams.get("at_id"));
		}
		if (jsonParams.get("atc_title") != null && !"".equals(jsonParams.get("atc_title").toString())) {
			builder.append(" AND atc.atc_title LIKE N'%" + jsonParams.get("atc_title").toString() + "%'");
			builderGetTotal
					.append(" AND atc.atc_title LIKE N'%" + jsonParams.get("atc_title").toString() + "%'");
		}
		// sortby
		if (jsonParams.get("sortField") != null && !"".equals(jsonParams.get("sortField").toString())) {
			switch (jsonParams.get("sortField").toString()) {
			default:
				builder.append(" ORDER BY atc.created_date DESC");
				break;
			}
			// sortOrder chỉ là descend và ascend hoặc rỗng
//			if (jsonParams.get("sortOrder") != null && "descend".equals(jsonParams.get("sortOrder").toString())) {
//				builder.append(" DESC");
//			}
//			if (jsonParams.get("sortOrder") != null && "ascend".equals(jsonParams.get("sortOrder").toString())) {
//				builder.append(" ASC");
//			}
		}
		// lấy các biến từ table (limit, offset)
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
			data.put("msg", "Lấy danh sách bài viết thất bại");
		}
		return data;
	}

	@Override
	public JSONObject delete(String article, int user_id) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(article);
		String query = "DELETE FROM article_topic_content WHERE article_topic_content.atc_id IN ("
				+ jsonParams.get("atc_id") + ")";
		try {
			int row = this.jdbcTemplate.update(query);
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			// result.put("msg", "Xóa b);
			result.put("msg", "Xóa bài viết thất bại");
		}
		return result;
	}

	@Override
	public JSONObject remove(String article, int user_id) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(article);
		// Sẽ phải check bên place địa điểm đã sử dụng ở bản ghi nào chưa
		try {
			String query = "UPDATE article_topic_content SET article_topic_content.deleted = 1, article_topic_content.modify_date = ?, "
					+ "article_topic_content.modify_by = ? WHERE article_topic_content.atc_id = ?";
			int row = this.jdbcTemplate.update(query,
					new Object[] { mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"), user_id,
							jsonParams.get("atc_id") });
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", e.getMessage());
			// result.put("msg", "Chuyển loại địa điểm vào thùng rác thất bại");
		}
		return result;
	}

	@Override
	public JSONObject restore(String article, int user_id) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(article);
		String sql = "UPDATE article_topic_content SET article_topic_content.deleted = 0, "
				+ "article_topic_content.modify_date = ?, article_topic_content.modify_by = ?"
				+ " WHERE article_topic_content.atc_id IN (" + jsonParams.get("atc_id") + ")";
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

	@Override
	public JSONObject getArticleById(String atc_id) {
		JSONObject result = new JSONObject();
		String query = "SELECT atc.at_id AS at_id, atc.atc_title, atc.atc_content "
				+ "FROM article_topic_content AS atc " + "WHERE atc.atc_id = " + atc_id;
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

	// Hàm xem article by id
	@Override
	public JSONObject viewArticleById(String atc_id) {
		JSONObject result = new JSONObject();
		String query = "SELECT atc.atc_title, atc.atc_content, user.full_name " + "FROM article_topic_content AS atc "
				+ "LEFT JOIN crm_user AS user ON atc.created_by = user.user_id " + "WHERE atc.atc_id = " + atc_id;
		try {
			Map<String, Object> articleObject = this.jdbcTemplate.queryForMap(query);

			result.put("success", true);
			result.put("data", articleObject);
		} catch (Exception e) {
			result.put("success", false);
			result.put("err", e.getMessage());
			result.put("msg", "Không lấy được thông tin bài viết. Kiểm tra lại");
		}
		return result;
	}
}
