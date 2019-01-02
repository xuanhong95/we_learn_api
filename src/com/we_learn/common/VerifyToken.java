package com.we_learn.common;

import org.json.simple.JSONObject;
import redis.clients.jedis.Jedis;

public class VerifyToken {
	public boolean isLogined = false;
	public String userId = "";
	public String userLogin = "";
	public String groupCode = "";
	/** 
	 * Hàm kiểm tra token có lưu trong Redis không
	 * Sử dụng thư viện Jedis Java
	 */
	public VerifyToken(String token) {
		this.isLogined = false;
		if (token != null && !token.isEmpty()) {
			String tokenKey = token.replaceFirst("Bearer ", "");
			// JedisPool jedisPool = new JedisPool("localhost");
			// Jedis jedis = jedisPool.getResource();
			Jedis jedis = new Jedis("localhost");
			String userInfo = jedis.get(tokenKey);
			if (userInfo != null && !userInfo.isEmpty()) {
				this.isLogined = true;
				MainUtility mainUtility = new MainUtility();
				JSONObject userObject = mainUtility.stringToJson(userInfo);
				this.userId = userObject.get("id").toString();
				this.userLogin = userObject.get("username").toString();
				this.groupCode = userObject.get("group_code").toString();
			}
			jedis.close();
		}
	}
	/** 
	 * Hàm kiểm tra user đã đăng nhập chưa
	 * @return result
	 */
	public JSONObject notFoundUser() {
		JSONObject result = new JSONObject();
		result.put("success", false);
		result.put("code", "401");
		result.put("error", "Invalid token");
		result.put("msg", "not found user");
		return result;
	}
	
	/** 
	 * Hàm kiểm tra có tồn tại đường link hay không
	 * @return result
	 */
	public JSONObject notFoundObject() {
		JSONObject result = new JSONObject();
		result.put("success", false);
		result.put("code", "404");
		result.put("error", "Bản ghi không tồn tại");
		return result;
	}
}
