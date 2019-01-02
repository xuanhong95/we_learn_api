package com.we_learn.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.we_learn.common.MainUtility;

public class PermissionDaoImpl implements PermissionDao {

	private JdbcTemplate jdbcTemplate;
	private Logger logger = Logger.getLogger(PermissionDaoImpl.class);
	private PlatformTransactionManager transactionManager;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setTransactionManager(PlatformTransactionManager txManager) {
		this.transactionManager = txManager;
	}

	@Override
	public JSONObject getPermissionTreeByGroupId(String group_id) {
		JSONObject result = new JSONObject();
		String query = "SELECT gp.permission_id FROM group_permission AS gp "
				+ "INNER JOIN permission AS p ON gp.permission_id = p.permission_id WHERE gp.deleted <> 1 AND gp.group_id = "
				+ group_id
				+ " AND p.permission_code NOT IN(SELECT parent.parent_code FROM permission AS parent WHERE parent.parent_code IS NOT NULL)";
		try {
//			Thread.sleep(3000);
			List<Map<String, Object>> lstPer = this.jdbcTemplate.queryForList(query);
//			List<Map<String, Object>> lstPer2 = this.jdbcTemplate.queryForList("SELECT * FROM crm_wards; SELECT * FROM crm_district; SELECT * FROM group_permission");
			List<String> lstId = new ArrayList<>();
			for (Map<String, Object> item : lstPer) {
				lstId.add(item.get("permission_id").toString());
			}
			result.put("success", true);
			result.put("data", lstId);

		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", e.getMessage());
		}
		return result;
	}

	@Override
	public JSONObject getAllPermissionTree() {
		JSONObject result = new JSONObject();
		String query = "SELECT permission_id, permission_name,permission_code, parent_code, disabled FROM permission WHERE deleted <> 1 ORDER BY -priority DESC, roles_id ASC";
		try {
			List<Map<String, Object>> lstPermission = this.jdbcTemplate.queryForList(query);
			List<Map<String, Object>> lstPermissionTree = new ArrayList<>();
			List<Map<String, Object>> lstChildren;
			Map<String, Object> permissionTree;
			for (Map<String, Object> item : lstPermission) {
				if (item.get("parent_code") == null || item.get("parent_code").toString().isEmpty()) {
					permissionTree = new HashMap<>();
					permissionTree.put("title", item.get("permission_name"));
					permissionTree.put("key", item.get("permission_id"));
					// permissionTree.put("key", item.get("permission_code"));
					lstChildren = findChildren(lstPermission, item);
					if (lstChildren.size() > 0) {
						permissionTree.put("children", lstChildren);
					}
					lstPermissionTree.add(permissionTree);
				}
			}
			result.put("success", true);
			result.put("data", lstPermissionTree);
		} catch (Exception e) {
			// TODO: handle exception
			result.put("success", false);
			result.put("msg", e.getMessage());
		}
		return result;
	}

	@Override
	public JSONObject updatePermission(String gp, int user_id, String user_name) {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		JSONObject jsonParams = mainUtil.stringToJson(gp);
		String sqlDeleteAllPermission = "DELETE FROM group_permission WHERE group_id = ?";
		String sqlCheckIsParent = "SELECT EXISTS(SELECT 1 FROM permission AS child WHERE child.deleted <> 1 "
				+ "AND child.parent_code = (SELECT parent.permission_code FROM permission AS parent WHERE parent.deleted <> 1 AND parent.permission_id = ?))";
		String sqlUpdate = "INSERT INTO group_permission (group_id, permission_id, create_date, create_by, deleted) "
				+ "VALUES (?, ?, ?, ?, ?)";
		try {
			this.jdbcTemplate.update(sqlDeleteAllPermission, new Object[] { jsonParams.get("group_id") });
			JSONArray lstPer = (JSONArray) jsonParams.get("lst_permision");
			addParentToUpdate(lstPer);
			for (int i = 0; i < lstPer.size(); i++) {
				this.jdbcTemplate.update(sqlUpdate, new Object[] { jsonParams.get("group_id"), lstPer.get(i),
						mainUtil.dateToStringFormat(new Date(), "yyyy-MM-dd HH:mm:ss"), user_id, 0 });
				// if (this.jdbcTemplate.queryForObject(sqlCheckIsParent, new Object[] {
				// lstPer.get(i) },
				// Integer.class) == 0) {

				// }
			}
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", e.getMessage());
		}
		return result;
	}

	private void addParentToUpdate(JSONArray lstPer) {
		String sqlGetParent = "SELECT permission_id FROM permission WHERE deleted <> 1 "
				+ "AND permission_code = (SELECT child.parent_code FROM permission AS child WHERE child.deleted <> 1 AND child.permission_id = ?)";
		for (int i = 0; i < lstPer.size(); i++) {
			List<Map<String, Object>> lstParent = this.jdbcTemplate.queryForList(sqlGetParent,
					new Object[] { lstPer.get(i) });
			String parent_id = lstParent.size() > 0 ? lstParent.get(0).get("permission_id").toString() : "0";
			if (!parent_id.equals("0") && !lstPer.contains(parent_id)) {
				lstPer.add(parent_id);
			}
		}
	}

	private List<Map<String, Object>> findChildren(List<Map<String, Object>> lstPermission,
			Map<String, Object> permission) {
		List<Map<String, Object>> lstChildrenResult = new ArrayList<>();
		List<Map<String, Object>> lstChildren;
		Map<String, Object> children;
		for (Map<String, Object> item : lstPermission) {
			if (permission.get("permission_code").equals(item.get("parent_code"))) {
				children = new HashMap<>();
				children.put("title", item.get("permission_name"));
				children.put("key", item.get("permission_id"));
				// children.put("key", item.get("permission_code"));
				lstChildren = findChildren(lstPermission, item);
				if (lstChildren.size() > 0) {
					children.put("children", lstChildren);
				}
				lstChildrenResult.add(children);
			}
		}
		return lstChildrenResult;
	}
	
	@Override
	public JSONObject getAllGroupSelect() {
		JSONObject result = new JSONObject();
		MainUtility mainUtil = new MainUtility();
		String query = "SELECT crm_group.group_id, crm_group.group_name, crm_group.group_code FROM crm_group WHERE deleted <> 1";
		try {
			List<Map<String, Object>> lstGroup = this.jdbcTemplate.queryForList(query);
			result.put("success", true);
			result.put("data", lstGroup);
		} catch (Exception e) {
			// TODO: handle exception
			result.put("success", false);
			result.put("msg", e.getMessage());
		}
		return result;
	}
}
