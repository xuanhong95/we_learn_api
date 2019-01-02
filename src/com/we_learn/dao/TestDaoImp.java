package com.we_learn.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import com.we_learn.common.MainUtility;

public class TestDaoImp implements TestDao{
	
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
		String name = jsonParams.get("name").toString();
		String type = jsonParams.get("type").toString();
		String query = "INSERT INTO `test`(`test_name`, `test_type`, `created_by`) VALUES (?,?,?)";
		try {
			Object[] objects = new Object[] {name, type, user_id};
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
	public JSONObject update(String param, String user_id) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String name = jsonParams.get("name").toString();
		String type = jsonParams.get("type").toString();
		String test_id = jsonParams.get("test_id").toString();
		String query = "UPDATE `test` SET `test_name`=?,`test_type`=?,`modify_date`=?,`modify_by`=? WHERE `test_id` = ?";
		//insert
		try {
			String dateTimeNow = mainUtil.getDateFormat("yyyy-MM-dd HH:mm:ss", new Date());
			Object[] objects = new Object[] {name, type, dateTimeNow, user_id, test_id};
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
	public JSONObject getById(String test_id) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
//		String queryForTest = "SELECT `test_id`, `test_name`, `test_type` FROM `test` WHERE `test_id` = ?";
		String queryForQuestion = "SELECT `tq_id`, `tq_content` FROM `test_question` WHERE `test_id` = ? AND deleted = 0";
		String queryForAnwser = "SELECT `ta_id`, `ta_content` FROM `test_answer` WHERE `tq_id` = ? AND deleted = 0";
		try {
//			Map<String, Object> test = this.jdbcTemplate.queryForMap(queryForTest, new Object[] {test_id});
			List<Map<String, Object>> lstQuestion = this.jdbcTemplate.queryForList(queryForQuestion, new Object[] {test_id});
			for (Map<String, Object> question : lstQuestion) {
				String tq_id = question.get("tq_id").toString();
				List<Map<String, Object>> lstAnwser = this.jdbcTemplate.queryForList(queryForAnwser, new Object[] {tq_id});
				question.put("lst_anwser", lstAnwser);
			}
//			test.put("results", value)
			JSONObject results = new JSONObject();
			results.put("results", lstQuestion);
			results.put("total", lstQuestion.size());
			result.put("data", results);
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("err", e.getMessage());
			result.put("msg", "Lấy danh sách bài kiểm tra thất bại");
		}
		return result;
	}

	@Override
	public JSONObject getAll(String user_id) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		String query = "SELECT test.test_id, test.test_name, test.test_type, DATE_FORMAT(test.created_date, '%d-%m-%Y') AS created_date, "
				+ "test.created_by, (SELECT COUNT(ques.test_id) "
				+ "FROM test_question AS ques WHERE ques.test_id = test.test_id) AS question_number, "
				+ "IF(us.result IS NULL,0, us.result) AS last_point, IF(us.result IS NULL,0, 1) AS status "
				+ "FROM test LEFT JOIN user_result us ON (us.test_id = test.test_id AND us.created_by = ?) WHERE test.deleted = 0";
		try {
			List<Map<String, Object>> listTest = this.jdbcTemplate.queryForList(query, new Object[] {user_id});
			JSONObject results = new JSONObject();
			result.put("data", listTest);
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("err", e.getMessage());
			result.put("msg", "Lấy danh sách bài kiểm tra thất bại");
		}
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
	public JSONObject getCorrectAnswerById(String param) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(param);
		String test_id = jsonParams.get("test_id").toString();
		String query = "SELECT tq.tq_id, ta.ta_content AS correct_answer FROM `test` "
				+ "LEFT JOIN test_question tq ON (tq.test_id = test.test_id) "
				+ "LEFT JOIN correct_answer ca ON (ca.tq_id = tq.tq_id) "
				+ "LEFT JOIN test_answer ta ON (ca.ta_id = ta.ta_id) "
				+ "WHERE test.test_id = ?";
		try {
			List<Map<String, Object>> listTest = this.jdbcTemplate.queryForList(query, new Object[] {test_id});
			JSONObject results = new JSONObject();
			result.put("data", listTest);
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("err", e.getMessage());
			result.put("msg", "Lấy danh sách bài kiểm tra thất bại");
		}
		return result;
	}

	
	
}
