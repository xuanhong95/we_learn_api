package com.we_learn.dao;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.we_learn.dao.UserDaoImpl;
import com.mysql.jdbc.Statement;
import com.we_learn.common.MainUtility;

public class UserDaoImpl implements UserDao {
	private JdbcTemplate jdbcTemplate;
	private Logger logger = Logger.getLogger(UserDaoImpl.class);

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


	@Override
	public JSONObject getUserByPage(String param) {
		// TODO Auto-generated method stub
		JSONObject data = new JSONObject();
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		StringBuilder builder = new StringBuilder();
		StringBuilder builderGetTotal = new StringBuilder();

		builder.append("SELECT user_id, user_login, full_name, email, group_id "
				+ "FROM crm_user "
				+ "WHERE group_id IN (2,3) ");
		builderGetTotal.append("SELECT COUNT(1) FROM crm_user "
				+ "WHERE group_id IN (2,3) ");
		if (jsonParams.get("user_login") != null && !"".equals(jsonParams.get("user_login").toString())) {
			builder.append(" AND user_login LIKE N'%" + jsonParams.get("user_login").toString() + "%'");
			builderGetTotal
					.append(" AND user_login LIKE N'%" + jsonParams.get("user_login").toString() + "%'");
		}
		if (jsonParams.get("full_name") != null && !"".equals(jsonParams.get("full_name").toString())) {
			builder.append(" AND full_name LIKE N'%" + jsonParams.get("full_name").toString() + "%'");
			builderGetTotal
			.append(" AND full_name LIKE N'%" + jsonParams.get("full_name").toString() + "%'");
		}
		if (jsonParams.get("email") != null && !"".equals(jsonParams.get("email").toString())) {
			builder.append(" AND email LIKE N'%" + jsonParams.get("email").toString() + "%'");
			builderGetTotal
			.append(" AND email LIKE N'%" + jsonParams.get("email").toString() + "%'");
		}
		// sortby
		if (jsonParams.get("sortField") != null && !"".equals(jsonParams.get("sortField").toString())) {
			switch (jsonParams.get("sortField").toString()) {
			default:
				builder.append(" ORDER BY crm_user.created_date DESC");
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
			List<Map<String, Object>> listUser = this.jdbcTemplate.queryForList(builder.toString());
			JSONObject results = new JSONObject();
			results.put("results", listUser);
			results.put("total", totalRow);
			data.put("data", results);
			data.put("success", true);
		} catch (Exception e) {
			data.put("success", false);
			data.put("err", e.getMessage());
			data.put("msg", "Lấy danh sách người dùng thất bại");
		}
		return data;
	}
	
	public JSONObject activePremium(String params) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(params);
		String sqlUpdate = "UPDATE crm_user SET "
				+ "premium_date = DATE_ADD(DATE(NOW()), INTERVAL ? DAY), "
				+ "group_id = 3 WHERE user_id = ?";
		try {
			if (this.jdbcTemplate.update(sqlUpdate,
					new Object[] { jsonParams.get("days"), jsonParams.get("user_id") }) == 0) {
				result.put("success", false);
				result.put("msg", "Lỗi khi nâng cấp tài khoản premium");
				return result;
			}
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", "Lỗi khi nâng cấp tài khoản premium");
			result.put("err", e.getMessage());
		}

		return result;
	}
	
	@Override
	public JSONObject updateUserPassword(String user, int user_id) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(user);

		try {
			String oldPassword = jsonParams.get("old_password").toString();
			MessageDigest md = MessageDigest.getInstance("md5");
			md.update(oldPassword.getBytes());
			byte[] digest = md.digest();
			String oldPasswordMd5 = DatatypeConverter.printHexBinary(digest).toLowerCase();
			String sqlCheckPassword;
			sqlCheckPassword = "SELECT EXISTS (SELECT 1 FROM crm_user WHERE password = '" + oldPasswordMd5
					+ "' AND user_id = " + user_id + ")";
			if (this.jdbcTemplate.queryForObject(sqlCheckPassword, Integer.class) != 1) {
				result.put("success", false);
				result.put("msg", "Sai mật khẩu cũ! Kiểm tra lại");
				return result;
			}
			if (jsonParams.get("new_password") == null || "".equals(jsonParams.get("new_password").toString())) {
				result.put("success", false);
				result.put("msg", "Mật khẩu mới không được để trống");
				return result;
			}
			String newPassword = jsonParams.get("new_password").toString();
			md.update(newPassword.getBytes());
			digest = md.digest();
			String newPasswordMd5 = DatatypeConverter.printHexBinary(digest).toLowerCase();
			jsonParams.put("new_password", newPasswordMd5);
			String sql = "UPDATE crm_user SET password = ?, modify_date = ?, modify_by = ? WHERE crm_user.user_id = ?";

			this.jdbcTemplate.execute(sql, new PreparedStatementCallback<Boolean>() {
				@Override
				public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException {
					ps.setString(1, jsonParams.get("new_password").toString());
					ps.setString(2, mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"));
					ps.setInt(3, user_id);
					ps.setInt(4, user_id);
					return ps.execute();
				}
			});
			result.put("success", true);
		} catch (Exception e) {
			logger.info(e.getMessage());
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", e.getMessage());
		}
		return result;
	}
}
