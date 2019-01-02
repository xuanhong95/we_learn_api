package com.we_learn.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONObject;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.we_learn.common.MainUtility;

import redis.clients.jedis.Jedis;

public class LoginDaoImp implements LoginDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	private JavaMailSender mailSender;

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public JSONObject login(String params) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(params);
		String password = jsonParams.get("password").toString();
		String passwordMd5 = "";
		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			md.update(password.getBytes());
			byte[] digest = md.digest();
			passwordMd5 = DatatypeConverter.printHexBinary(digest).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
		}

		String query = "SELECT crm_user.*, crm_group.group_code FROM crm_user LEFT JOIN crm_group ON crm_user.group_id = crm_group.group_id"
				+ " WHERE user_login = ? AND password = ? AND active_status = 1";
		try {
			Map<String, Object> user = this.jdbcTemplate.queryForMap(query,
					new Object[] { jsonParams.get("username").toString(), passwordMd5 });
			Date date = new Date();
			long currentTimeMillis = System.currentTimeMillis();
			Date expireDate = new Date(currentTimeMillis + (24 * 60 * 60 * 10 * 1000));
			Algorithm algorithm = Algorithm.HMAC256("biPHxAMbw7H0mUfV3xO1TIpv0nAQfK41");

			String token = JWT.create().withClaim("id", user.get("user_id").toString())
					.withClaim("username", user.get("user_login").toString())
					.withClaim("group_code", user.get("group_code").toString()).withIssuedAt(date)
					.withExpiresAt(expireDate).sign(algorithm);

			Jedis jedis = new Jedis("localhost");
			jedis.get(token);
			String queryGetAllPermission = "SELECT p.permission_code FROM group_permission AS gp "
					+ "INNER JOIN permission AS p ON gp.permission_id = p.permission_id "
					+ "INNER JOIN crm_group ON gp.group_id = crm_group.group_id WHERE gp.deleted <> 1 AND crm_group.group_code = ?";
			List<Map<String, Object>> lstPermission = this.jdbcTemplate.queryForList(queryGetAllPermission,
					new Object[] { user.get("group_code").toString() });
			List<String> arrPermission = new ArrayList<>();
			for (Map<String, Object> i : lstPermission) {
				arrPermission.add(i.get("permission_code").toString());
			}

			JSONObject userJson = new JSONObject();
			userJson.put("id", user.get("user_id"));
			userJson.put("username", user.get("user_login"));
			userJson.put("user_login", user.get("user_login"));
			userJson.put("group_code", user.get("group_code"));
			// userJson.put("lst_permission", arrPermission);
			jedis.set(token, userJson.toString());
			jedis.close();

			userJson.put("username", user.get("full_name"));
			result.put("success", true);
			result.put("data", userJson);
			result.put("lstPermission", arrPermission);
			result.put("Authorization", "Bearer " + token);
		} catch (EmptyResultDataAccessException e) {
			result.put("success", false);
			result.put("msg", "Login fail.");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", e.getMessage());
		}
		return result;
	}

	@Override
	public JSONObject logout(String token) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		String tokenKey = token.replaceFirst("Bearer ", "");
		Jedis jedis = new Jedis("localhost");
		jedis.del(tokenKey);
		result.put("success", true);
		result.put("data", "Bye see you again!");
		return result;
	}

	@Override
	public JSONObject signUp(String params, String rootUrl) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(params);
		String sqlCheckExists;
		try {
			String passwordMd5 = "";
			sqlCheckExists = "SELECT EXISTS (SELECT 1 FROM crm_user WHERE (user_login = N'"
					+ jsonParams.get("user_login").toString() + "'))";
			if (this.jdbcTemplate.queryForObject(sqlCheckExists, Integer.class) == 1) {
				result.put("success", false);
				result.put("msg", "Tài khoản đã tồn tại. Kiểm tra lại");
				return result;
			}
			String queryCheckEmailExists = "SELECT EXISTS (SELECT 1 FROM crm_user WHERE (email = N'"
					+ jsonParams.get("email").toString() + "'))";
			if (this.jdbcTemplate.queryForObject(queryCheckEmailExists, Integer.class) == 1) {
				result.put("success", false);
				result.put("msg", "Email đã được sử dụng. Kiểm tra lại");
				return result;
			}
			String password = jsonParams.get("password").toString();
			try {
				MessageDigest md = MessageDigest.getInstance("md5");
				md.update(password.getBytes());
				byte[] digest = md.digest();
				passwordMd5 = DatatypeConverter.printHexBinary(digest).toLowerCase();
				jsonParams.put("password", passwordMd5);
			} catch (NoSuchAlgorithmException e) {
			}
			String sqlInsertUser = "INSERT INTO crm_user (user_login, full_name, password, email, group_id, create_date, "
					+ "deleted, code_active, expired_active)" + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			String codeActive = UUID.randomUUID().toString();
			KeyHolder holder = new GeneratedKeyHolder();
			this.jdbcTemplate.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(sqlInsertUser, Statement.RETURN_GENERATED_KEYS);
					int count = 1;
					mainUtil.setParam(ps, jsonParams.get("user_login"), "string", count++);
					mainUtil.setParam(ps, jsonParams.get("full_name"), "string", count++);
					mainUtil.setParam(ps, jsonParams.get("password"), "string", count++);
					mainUtil.setParam(ps, jsonParams.get("email"), "string", count++);
					mainUtil.setParam(ps, 2, "int", count++);
					mainUtil.setParam(ps, mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"), "string",
							count++);
					mainUtil.setParam(ps, 0, "int", count++);
					mainUtil.setParam(ps, codeActive, "string", count++);
					mainUtil.setParam(ps, mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"), "string",
							count++);
					return ps;
				}
			}, holder);
			if (holder.getKey().intValue() > 0) {
				// gửi mail active
				String subject = "Thông báo kích hoạt tài khoản: " + jsonParams.get("user_login");
				StringBuilder content;
				content = new StringBuilder();
				content.append("Click <a href='" + rootUrl + "#/active-account?user="
						+ jsonParams.get("user_login").toString() + "&code=" + codeActive
						+ "'> tại đây </a> để kích hoạt tài khoản.");
				this.sendMimeEmail(jsonParams.get("email").toString(), subject, content.toString());
				result.put("success", true);
			} else {
				result.put("success", false);
				result.put("msg", "Xảy ra lỗi khi kích hoạt tài khoản. Vui lòng kiểm tra lại thông tin");
			}
		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", "Xảy ra lỗi khi kích hoạt tài khoản. Vui lòng kiểm tra lại thông tin");
			result.put("err", e.getMessage());
		}
		return result;
	}

	@Override
	public JSONObject activeAccount(String params) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(params);
		String sqlCheckExists;
		try {
			String validateInfo = "SELECT EXISTS (SELECT 1 FROM crm_user WHERE user_login = ? AND code_active = ?)";
			String sqlUpdateInfo = "UPDATE crm_user SET code_active = ?, active_status = ?, expired_active = ?"
					+ " WHERE user_login = ?";
			if (this.jdbcTemplate.queryForObject(validateInfo,
					new Object[] { jsonParams.get("user_login"), jsonParams.get("code_active") }, Integer.class) == 0) {
				result.put("success", false);
				result.put("msg", "Dữ liệu không hợp lệ hoặc hết thời gian kích hoạt tài khoản");
				return result;
			}
			Object[] newObj = new Object[] { null, 1, null, jsonParams.get("user_login") };
			if (this.jdbcTemplate.update(sqlUpdateInfo, newObj) == 0) {
				result.put("success", false);
				result.put("msg", "Lỗi khi kích hoạt tài khoản. Vui lòng kiểm tra lại");
				return result;
			}
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", "Lỗi khi kích hoạt tài khoản. Vui lòng kiểm tra lại");
			result.put("err", e.getMessage());
		}
		return result;
	}

	private void sendMimeEmail(String toEmail, String subject, String content) {
		Thread thread = new Thread() {
			public void run() {
				MimeMessagePreparator detail = new MimeMessagePreparator() {
					public void prepare(MimeMessage mimeMessageObj) throws MessagingException {
						MimeMessageHelper messageObj = new MimeMessageHelper(mimeMessageObj, true, "UTF-8");
						messageObj.setTo(toEmail);
						messageObj.setSubject(subject);
						messageObj.setText(content, true);
					}
				};
				mailSender.send(detail);
			}
		};
		thread.start();
	}

	@Override
	public JSONObject resendActiveCode(String params, String rootUrl) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(params);
		String sqlCheckExists;
		try {
			String queryUserInfo = "SELECT user_login, full_name, active_status FROM crm_user WHERE email = ?";
			List<Map<String, Object>> lstAccount = this.jdbcTemplate.queryForList(queryUserInfo,
					new Object[] { jsonParams.get("email").toString() });
			if (lstAccount.size() == 0) {
				result.put("success", false);
				result.put("msg", "Email không tồn tại. Kiểm tra lại");
				return result;
			}
			if (lstAccount.get(0).get("active_status").toString().equals("1")) {
				result.put("success", false);
				result.put("msg", "Tài khoản sử dụng email này đã được kích hoạt. Kiểm tra lại");
				return result;
			}
			String sqlUpdateInfo = "UPDATE crm_user SET code_active = ?" + " WHERE user_login = ?";
			String codeActive = UUID.randomUUID().toString();
			Object[] newObj = new Object[] { codeActive, lstAccount.get(0).get("user_login") };
			this.jdbcTemplate.update(sqlUpdateInfo, newObj);
			
			// gá»­i mail active
			String subject = "Thông báo kích hoạt tài khoản: " + lstAccount.get(0).get("user_login");
			StringBuilder content;
			content = new StringBuilder();
			content.append("Click <a href='" + rootUrl + "#/active-account?user="
					+ lstAccount.get(0).get("user_login") + "&code=" + codeActive
					+ "'> Tại đây </a> để kích hoạt tài khoản.");
			this.sendMimeEmail(jsonParams.get("email").toString(), subject, content.toString());
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", "Xảy ra lỗi. Vui lòng kiểm tra lại thông tin");
			result.put("err", e.getMessage());
		}
		return result;
	}
}
