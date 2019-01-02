package com.we_learn.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.we_learn.dao.CreateTestDaoImpl;
import com.mysql.jdbc.Statement;
import com.we_learn.common.MainUtility;

public class CreateTestDaoImpl implements CreateTestDao {
	private JdbcTemplate jdbcTemplate;
	private PlatformTransactionManager transactionManager;
	private Logger logger = Logger.getLogger(CreateTestDaoImpl.class);

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public void setTransactionManager(PlatformTransactionManager txManager) {
		this.transactionManager = txManager;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public JSONObject insert(String param, int user_id) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		 TransactionDefinition txDef = new DefaultTransactionDefinition();
		 TransactionStatus txStatus = this.transactionManager.getTransaction(txDef);
		// insert

		String sqlInsertTest = "INSERT INTO test (test_name, test_type, created_date, created_by, deleted)"
				+ " VALUES (?,?,?,?,?)";
		String sqlInsertTestQuestion = "INSERT INTO test_question (test_id, tq_content,"
				+ " created_date, created_by, deleted)" + " VALUES (?,?,?,?,?)";
		String sqlInsertTestAnswer = "INSERT INTO test_answer (tq_id, ta_content," + " created_date, created_by, deleted)"
				+ " VALUES (?,?,?,?,?)";
		String sqlInsertCorrectAnswer = "INSERT INTO correct_answer (test_id, tq_id, ta_id,"
				+ " created_date, created_by, deleted)" + " VALUES (?,?,?,?,?,?)";
		try {
			KeyHolder holder = new GeneratedKeyHolder();
			int rowTest = this.jdbcTemplate.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(sqlInsertTest, Statement.RETURN_GENERATED_KEYS);
					int count = 1;
					mainUtil.setParamJSONObject(ps, jsonParams, "test_name", "string", count++);
					// Tạm set type = 1 (bài kiểm tra reading)
					ps.setInt(count++, 1);
					ps.setString(count++, mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"));
					ps.setInt(count++, user_id);
					ps.setInt(count++, 0);
					return ps;
				}
			}, holder);

			// Insert test_question
			JSONArray lstQuestion = (JSONArray) jsonParams.get("lstQuestion");
			GeneratedKeyHolder quesHolder;
			for (Object questionObj : lstQuestion) {
				JSONObject question = (JSONObject) questionObj;
				quesHolder = new GeneratedKeyHolder();
				int rowQues = this.jdbcTemplate.update(new PreparedStatementCreator() {
					@Override
					public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
						PreparedStatement statement = con.prepareStatement(sqlInsertTestQuestion,
								Statement.RETURN_GENERATED_KEYS);
						int count = 1;
						statement.setInt(count++, holder.getKey().intValue());
						mainUtil.setParamJSONObject(statement, question, "tq_content", "string", count++);
						statement.setString(count++, mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"));
						statement.setInt(count++, user_id);
						statement.setInt(count++, 0);
						return statement;
					}
				}, quesHolder);
				
				if (rowQues == 0) {
					this.transactionManager.rollback(txStatus);
					result.put("success", false);
					result.put("msg", "Lỗi khi tạo đề thi");
					return result;
				}

				int questionHolder = quesHolder.getKey().intValue();
				JSONArray lst_answer = (JSONArray) question.get("lst_answer");
				GeneratedKeyHolder answerHolder;
				for (Object ansObj : lst_answer) {
					JSONObject answer = (JSONObject) ansObj;
					answerHolder = new GeneratedKeyHolder();
					int rowAns = this.jdbcTemplate.update(new PreparedStatementCreator() {
						@Override
						public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
							PreparedStatement statement = con.prepareStatement(sqlInsertTestAnswer,
									Statement.RETURN_GENERATED_KEYS);
							int count = 1;
							statement.setInt(count++, questionHolder);
							mainUtil.setParamJSONObject(statement, answer, "ta_content", "string", count++);
							statement.setString(count++,
									mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"));
							statement.setInt(count++, user_id);
							statement.setInt(count++, 0);
							return statement;
						}
					}, answerHolder);
					
					if (rowAns == 0) {
						this.transactionManager.rollback(txStatus);
						result.put("success", false);
						result.put("msg", "Lỗi khi tạo đề thi");
						return result;
					}

					if (Integer.parseInt(question.get("correct_answer").toString()) == Integer
							.parseInt(answer.get("rowKey").toString())) {
						int ansHolder = answerHolder.getKey().intValue();
						KeyHolder correctAnswerHolder = new GeneratedKeyHolder();
						int rowCorrect = this.jdbcTemplate.update(new PreparedStatementCreator() {
							@Override
							public PreparedStatement createPreparedStatement(Connection connection)
									throws SQLException {
								PreparedStatement ps = connection.prepareStatement(sqlInsertCorrectAnswer,
										Statement.RETURN_GENERATED_KEYS);
								int count = 1;
								ps.setInt(count++, holder.getKey().intValue());
								ps.setInt(count++, questionHolder);
								ps.setInt(count++, ansHolder);
								ps.setString(count++, mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"));
								ps.setInt(count++, user_id);
								ps.setInt(count++, 0);
								return ps;
							}
						}, correctAnswerHolder);
						if (rowCorrect == 0) {
							this.transactionManager.rollback(txStatus);
							result.put("success", false);
							result.put("msg", "Lỗi khi tạo đề thi");
							return result;
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			this.transactionManager.rollback(txStatus);
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", e.getMessage());
			return result;
		}
		this.transactionManager.commit(txStatus);
		result.put("success", true);
		return result;
	}

	@Override
	public JSONObject getTestByPage(String param) {
		// TODO Auto-generated method stub
		JSONObject data = new JSONObject();
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		StringBuilder builder = new StringBuilder();
		StringBuilder builderGetTotal = new StringBuilder();

		builder.append("SELECT test.test_id, test.test_name, "
				+ "test.deleted, user.full_name, "
				+ "(SELECT COUNT(test_question.tq_id) FROM test_question "
				+ "WHERE test_question.test_id = test.test_id) AS question_number, "
				+ "IF(test.created_date IS NULL,null, DATE_FORMAT(test.created_date, '%d-%m-%Y')) AS created_date FROM test "
				+ "LEFT JOIN crm_user AS user ON test.created_by = user.user_id WHERE 1=1 ");
		builderGetTotal.append("SELECT COUNT(1) FROM test "
				+ "LEFT JOIN crm_user AS user ON test.created_by = user.user_id ");
		// filter header
//		if (jsonParams.get("status") == null || Integer.parseInt(jsonParams.get("status").toString()) == -1) {
//			builder.append(" AND article.deleted <> 1");
//			builderGetTotal.append(" AND article.deleted <> 1");
//		} else if (Integer.parseInt(jsonParams.get("status").toString()) == -2) {// thùng rác
//			builder.append(" AND article.deleted = 1");
//			builderGetTotal.append(" AND article.deleted = 1");
//		}
//		if (Integer.parseInt(jsonParams.get("article_type").toString()) > -1) {
//			builder.append(" AND article.type_id=" + jsonParams.get("article_type"));
//			builderGetTotal.append(" AND article.type_id=" + jsonParams.get("article_type"));
//		}
		if (jsonParams.get("test_name") != null && !"".equals(jsonParams.get("test_name").toString())) {
			builder.append(" AND test.test_name LIKE N'%" + jsonParams.get("test_name").toString() + "%'");
			builderGetTotal
					.append(" AND test.test_name LIKE N'%" + jsonParams.get("test_name").toString() + "%'");
		}
		// sortby
		if (jsonParams.get("sortField") != null && !"".equals(jsonParams.get("sortField").toString())) {
			switch (jsonParams.get("sortField").toString()) {
			default:
				builder.append(" ORDER BY test.created_date DESC");
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
			List<Map<String, Object>> listTest = this.jdbcTemplate.queryForList(builder.toString());
			JSONObject results = new JSONObject();
			results.put("results", listTest);
			results.put("total", totalRow);
			data.put("data", results);
			data.put("success", true);
		} catch (Exception e) {
			data.put("success", false);
			data.put("err", e.getMessage());
			data.put("msg", "Lấy danh sách đề thi thất bại");
		}
		return data;
	}

	@Override
	public JSONObject getTestById(String test_id) {
		JSONObject result = new JSONObject();
		String queryTestInfo = "SELECT test.test_name FROM test "
				+ "WHERE test.test_id = " + test_id;
		String queryTestQuestionData = "SELECT tq_id, test_id, tq_content FROM test_question "
				+ "WHERE test_id = " + test_id ;
		try {
			Map<String, Object> testObject = this.jdbcTemplate.queryForMap(queryTestInfo);
			List<Map<String, Object>> lstTestQuesData = this.jdbcTemplate.queryForList(queryTestQuestionData.toString());
			String queryForLstAnswer = "SELECT tq_id, ta_id, ta_content, ta_id AS rowKey "
					+ "FROM test_answer "
					+ "WHERE tq_id = ?";
			String queryForCorrectAnswer = "SELECT ta_id FROM correct_answer "
					+ "WHERE correct_answer.tq_id = ?";
			for (Map<String, Object> question : lstTestQuesData) {
				question.put("lst_answer", this.jdbcTemplate.queryForList(queryForLstAnswer,
						new Object[] { question.get("tq_id").toString() }));
				Map<String, Object> correct_answer = this.jdbcTemplate.queryForMap(queryForCorrectAnswer,
						new Object[] { question.get("tq_id").toString() });
				question.put("correct_answer", correct_answer.get("ta_id"));
			}
			
			JSONObject data = new JSONObject();
			data.put("testObject", testObject);
			data.put("lstQuestion", lstTestQuesData);
			result.put("success", true);
			result.put("data", data);
		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", e.getMessage());
			// result.put("msg", "Không tồn tại loại địa điểm");
		}
		return result;
	}
}
