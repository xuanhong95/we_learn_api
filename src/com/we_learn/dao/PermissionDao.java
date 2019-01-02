package com.we_learn.dao;

import org.json.simple.JSONObject;

public interface PermissionDao {
	public JSONObject getAllPermissionTree();
	public JSONObject getPermissionTreeByGroupId(String group_id);
	public JSONObject updatePermission(String gp, int user_id, String user_name);
	public JSONObject getAllGroupSelect();
}
