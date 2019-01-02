package com.we_learn.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.we_learn.common.MainUtility;

public class UserAnswerDaoImp implements UserAnswerDao{
	private JdbcTemplate jdbcTemplate;
	private Logger logger = Logger.getLogger(QADaoImpl.class);

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public JSONObject userAnswer(String param, String user_id) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		JSONArray user_answer = (JSONArray) jsonParams.get("user_answer");
		String test_id = jsonParams.get("test_id").toString();
		String queryForCorrectAnswer = "SELECT `ta_id` FROM `correct_answer` WHERE `tq_id` = ?";
		int correctTime = 0;
		try {
			for (int i = 0; i < user_answer.size(); i++) {
				JSONObject answer = (JSONObject) user_answer.get(i);
				int ta_id = this.jdbcTemplate.queryForObject(queryForCorrectAnswer, Integer.class, new Object[] {answer.get("tq_id").toString()});
				if (answer.get("us_choice") != null) {
					int us_choice = Integer.parseInt(answer.get("us_choice").toString()) ;
					if (us_choice == ta_id) {
						correctTime++;
					}
				}
				
			}
			//check insert or update
			String queryForCheck = "SELECT 1 FROM `user_result` WHERE `test_id` = ?";
			List<Map<String, Object>> lstCheck = this.jdbcTemplate.queryForList(queryForCheck, new Object[] {test_id});
			//insert or update user result and user_answer
			double testResult = (double) correctTime/user_answer.size();
			double testResultRound = (double) Math.round(testResult * 100) / 100;
			testResultRound = testResultRound * 10d;
			String queryForInsertUserResult = "";
			String sqlForInsertUserAnswer = "";
			if (lstCheck.size() > 0) {
				//update
				queryForInsertUserResult = "UPDATE `user_result` SET `result`=?,`modify_date`=?,`modify_by`=? "
						+ "WHERE `test_id` = ? AND `created_by` = ?";
				sqlForInsertUserAnswer = "UPDATE `user_answer` SET `us_choice`=?,`modify_date`=?,`modify_by`=? "
						+ "WHERE `tq_id` = ? AND `created_by` = ?";
				String dateTimeNow = mainUtil.getDateFormat("yyyy-MM-dd HH:mm:ss", new Date());
				int row = this.jdbcTemplate.update(queryForInsertUserResult, new Object[] {String.valueOf(testResultRound),dateTimeNow , user_id,
						test_id, user_id});
				this.jdbcTemplate.batchUpdate(sqlForInsertUserAnswer, new BatchPreparedStatementSetter() {
					
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						// TODO Auto-generated method stub
						JSONObject answer = (JSONObject) user_answer.get(i);
						if (answer.get("us_choice") != null) {
							ps.setString(1, answer.get("us_choice").toString());
						} else {
							ps.setNull(1, java.sql.Types.NULL);
						}
						ps.setString(2, dateTimeNow);
						ps.setString(3, user_id);
						ps.setString(4, answer.get("tq_id").toString());
						ps.setString(5, user_id);
					}
					
					@Override
					public int getBatchSize() {
						// TODO Auto-generated method stub
						return user_answer.size();
					}
				});
			} else {
				//insert
				queryForInsertUserResult = "INSERT INTO `user_result`(`test_id`, `result`, `created_by`) VALUES (?,?,?)";
				sqlForInsertUserAnswer = "INSERT INTO `user_answer`(`test_id`, `tq_id`, `us_choice`, `created_by`) VALUES (?,?,?,?)";
				int row = this.jdbcTemplate.update(queryForInsertUserResult, new Object[] {test_id, String.valueOf(testResultRound), user_id});
				
				
				this.jdbcTemplate.batchUpdate(sqlForInsertUserAnswer, new BatchPreparedStatementSetter() {
					
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						// TODO Auto-generated method stub
						JSONObject answer = (JSONObject) user_answer.get(i);
						ps.setString(1, test_id);
						ps.setString(2, answer.get("tq_id").toString());
						if (answer.get("us_choice") != null) {
							ps.setString(3, answer.get("us_choice").toString());
						} else {
							ps.setNull(3, java.sql.Types.NULL);
						}
						
						ps.setString(4, user_id);
					}
					
					@Override
					public int getBatchSize() {
						// TODO Auto-generated method stub
						return user_answer.size();
					}
				});
			}
			result.put("success", true);
			result.put("correct_anwser", correctTime);
			result.put("total", user_answer.size());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result.put("success", false);
			result.put("err", e.getMessage());
			result.put("msg", "Kiểm tra kết quả thất bại");
		}
		return result;
	}
}
