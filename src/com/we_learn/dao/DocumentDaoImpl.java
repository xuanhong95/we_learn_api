package com.we_learn.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.we_learn.common.MainUtility;

public class DocumentDaoImpl implements DocumentDao {
	private JdbcTemplate jdbcTemplate;
	private Logger logger = Logger.getLogger(QADaoImpl.class);

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public JSONObject insert(Map<String, Object> jsonParams, String user_id) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		String sqlInsertDoc = "INSERT INTO `document`(`file_name`,`file_path`, create_date, `create_by`) VALUES (?,?,?,?)";
		// insert
		try {
			GeneratedKeyHolder holder = new GeneratedKeyHolder();
			this.jdbcTemplate.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(sqlInsertDoc, Statement.RETURN_GENERATED_KEYS);
					int count = 1;
					mainUtil.setParam(ps, jsonParams.get("file_name"), "string", count++);
					mainUtil.setParam(ps, jsonParams.get("file_path"), "string", count++);
					mainUtil.setParam(ps, mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"), "string",
							count++);
					mainUtil.setParam(ps, user_id, "int", count++);
					return ps;
				}
			}, holder);
			result.put("success", true);
			result.put("doc_id", holder.getKey().intValue());
			result.put("msg", "Upload successfully");
		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		return result;
	}

	@Override
	public JSONObject getDocByPage(String param, String user_id, String group_code, String rootUrl) {
		JSONObject data = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		StringBuilder builder = new StringBuilder();
		StringBuilder builderGetTotal = new StringBuilder();
		builder.append("SELECT doc_id, file_name, file_path, CONCAT('" + rootUrl
				+ "', file_path) AS url FROM document WHERE 1 = 1");
		builderGetTotal.append("SELECT COUNT(1) FROM document WHERE 1 = 1");
		if (jsonParams.get("file_name") != null && !jsonParams.get("file_name").toString().isEmpty()) {
			builder.append(" AND file_name LIKE '%" + jsonParams.get("file_name") + "%'");
			builderGetTotal.append(" AND file_name LIKE '%" + jsonParams.get("file_name") + "%'");
		}
		// sortby
		if (jsonParams.get("sortField") != null && !"".equals(jsonParams.get("sortField").toString())) {
			switch (jsonParams.get("sortField").toString()) {
			case "file_name":
				builder.append(" ORDER BY file_name");
				break;
			case "create_date":
				builder.append(" ORDER BY create_date");
				break;
			default:
				builder.append(" ORDER BY create_date");
				break;
			}
			if (jsonParams.get("sortOrder") != null && "ascend".equals(jsonParams.get("sortOrder").toString())) {
				builder.append(" ASC");
			} else {
				builder.append(" DESC");
			}
		} else {
			builder.append(" ORDER BY create_date DESC");
		}

		mainUtil.getLimitOffset(builder, jsonParams);
		try {
			int totalRow = this.jdbcTemplate.queryForObject(builderGetTotal.toString(), Integer.class);
			List<Map<String, Object>> lstFile = this.jdbcTemplate.queryForList(builder.toString());
			JSONObject results = new JSONObject();
			results.put("results", lstFile);
			results.put("total", totalRow);
			data.put("data", results);
			data.put("success", true);
		} catch (Exception e) {
			data.put("success", false);
			data.put("err", e.getMessage());
			data.put("msg", "Lấy danh sách file thất bại");
		}
		return data;
	}

}
