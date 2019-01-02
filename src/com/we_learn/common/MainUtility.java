package com.we_learn.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.jdbc.core.JdbcTemplate;

public class MainUtility {
	public JSONObject stringToJson(String params) {
		JSONObject result = new JSONObject();
		JSONParser parser = new JSONParser();
		try {
			result = (JSONObject) parser.parse(params);
		} catch (ParseException e) {
			return null;
		}
		return result;
	}
	
	public String getDateFormat(String format, Date date) {
		DateFormat df = new SimpleDateFormat(format);
		try {
			return df.format(date);
		} catch (Exception e) {
			// TODO: handle exception
			return "";
		}
	}
	
	public int getPageIndex(String page) {
		int pageIndex = Integer.parseInt(page);
		int index = (pageIndex - 1) * 10;
		return index;
	}
	
	public JSONObject notFoundObject() {
		JSONObject result = new JSONObject();
		result.put("success", false);
		result.put("code", "404");
		result.put("error", "Bản ghi không tồn tại");
		return result;
	}
	
	public boolean checkPermission(String group_code, JdbcTemplate jdbcTemplate, String permissionStr) {
		boolean result = false;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT EXISTS (SELECT 1 FROM group_permission AS gp INNER JOIN permission AS p "
				+ "ON gp.permission_id = p.permission_id INNER JOIN crm_group AS g ON gp.group_id = g.group_id "
				+ "WHERE g.group_code = ? ");
		sql.append("AND p.permission_code = ? )");

		if (jdbcTemplate.queryForObject(sql.toString(), new Object[] { group_code, permissionStr },
				Integer.class) == 1) {
			result = true;
		}
		return result;
	}
	
	public JSONObject noPermissionObject(boolean redirect) {
		JSONObject result = new JSONObject();
		result.put("success", false);
		if (redirect)
			result.put("code", 404);
		result.put("msg", "Bạn không có quyền thao tác");
		return result;
	}
	
	public void getLimitOffset(StringBuilder builder, JSONObject jsonParams) {
		// nếu results không phải là số thì lấy mặc định = 10
		int results = (jsonParams.get("pageSize") != null) ? Integer.parseInt(jsonParams.get("pageSize").toString())
				: 10;
		builder.append(" LIMIT " + results);
		if (jsonParams.get("current") != null) {
			builder.append(" OFFSET " + (Integer.parseInt(jsonParams.get("current").toString()) - 1) * results);
		}
	}
	
	public String dateToStringFormat(Date date, String formatOutput) {
		String result = "";
		try {
			DateFormat df = new SimpleDateFormat(formatOutput);
			result = df.format(date);
		} catch (Exception e) {
			return result;
		}
		return result;
	}
	
	public void setParamJSONObject(PreparedStatement ps, JSONObject obj, String column, String type, int count)
			throws NumberFormatException, SQLException {
		switch (type.toLowerCase()) {
		case "int":
			if (obj.get(column) != null) {
				ps.setInt(count, Integer.parseInt(obj.get(column).toString()));
			} else {
				ps.setNull(count, java.sql.Types.INTEGER);
			}
			break;
		case "double":
			if (obj.get(column) != null) {
				ps.setDouble(count, Double.parseDouble(obj.get(column).toString()));
			} else {
				ps.setNull(count, java.sql.Types.DOUBLE);
			}
			break;
		case "boolean":
			if (obj.get(column) != null && Boolean.parseBoolean(obj.get(column).toString())) {
				ps.setInt(count, 1);
			} else {
				ps.setInt(count, 0);
			}
			break;
		default:
			if (obj.get(column) != null) {
				ps.setString(count, obj.get(column).toString());
			} else {
				ps.setNull(count, java.sql.Types.NVARCHAR);
			}
			break;
		}
	}

	public void setParamMapObject(PreparedStatement ps, Map<String, Object> obj, String column, String type, int count)
			throws NumberFormatException, SQLException {
		switch (type.toLowerCase()) {
		case "int":
			if (obj.get(column) != null) {
				ps.setInt(count, Integer.parseInt(obj.get(column).toString()));
			} else {
				ps.setNull(count, java.sql.Types.INTEGER);
			}
			break;
		case "double":
			if (obj.get(column) != null) {
				ps.setDouble(count, Double.parseDouble(obj.get(column).toString()));
			} else {
				ps.setNull(count, java.sql.Types.DOUBLE);
			}
			break;
		case "boolean":
			if (obj.get(column) != null && (boolean) obj.get(column)) {
				ps.setInt(count, 1);
			} else {
				ps.setInt(count, 0);
			}
			break;
		default:
			if (obj.get(column) != null) {
				ps.setString(count, obj.get(column).toString());
			} else {
				ps.setNull(count, java.sql.Types.NVARCHAR);
			}
			break;
		}
	}
	public void setParam(PreparedStatement ps, Object value, String type, int count)
			throws NumberFormatException, SQLException {
		switch (type.toLowerCase()) {
		case "int":
			if (value != null) {
				ps.setInt(count, Integer.parseInt(value.toString()));
			} else {
				ps.setNull(count, java.sql.Types.INTEGER);
			}
			break;
		case "double":
			if (value != null) {
				ps.setDouble(count, Double.parseDouble(value.toString()));
			} else {
				ps.setNull(count, java.sql.Types.DOUBLE);
			}
			break;
		case "boolean":
			if (value != null && Boolean.parseBoolean(value.toString())) {
				ps.setInt(count, 1);
			} else {
				ps.setInt(count, 0);
			}
			break;
		default:
			if (value != null) {
				ps.setString(count, value.toString());
			} else {
				ps.setNull(count, java.sql.Types.NVARCHAR);
			}
			break;
		}
	}
	/**
	 * Hàm lưu file
	 * 
	 * @param context
	 *            ServletContext để đọc file config
	 * @param path
	 *            : đường dẫn tương đối tới thư mục cần lưu
	 * @param bodyParts:
	 *            FormDataBodyPart của file
	 * @return: đường dẫn tương đối tới file(dùng cho việc lưu db)
	 */
	public String saveFile(ServletContext context, String path, FormDataBodyPart bodyParts) {
		Properties prop = new Properties();
		String result;
		String fileNameWithoutExt;
		String extFile;
		try {
			prop.load(new FileInputStream(context.getRealPath("/WEB-INF/classes/config.properties")));
			String rootPath = prop.getProperty("uploadPath");
			result = deAccent(
					new String(bodyParts.getContentDisposition().getFileName().getBytes("ISO-8859-1"), "UTF-8")
							.replace(";", ""));
			extFile = FilenameUtils.getExtension(result);
			fileNameWithoutExt = FilenameUtils.removeExtension(result);
			Path pathDir = Paths.get(rootPath + path);
			Path pathFile = Paths.get(rootPath + path + File.separator + result);
			BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyParts.getEntity();
			if (!Files.exists(pathDir)) {
				Files.createDirectories(pathDir);
			}
			// trùng thì + thêm số vào tên
			int count = 0;
			while (Files.exists(pathFile)) {
				result = fileNameWithoutExt + "_" + (++count) + "." + extFile;
				pathFile = Paths.get(rootPath + path + File.separator + result);
			}
			Files.copy(bodyPartEntity.getInputStream(), pathFile);
		} catch (IOException ie) {
			return null;
		}
		return result;
	}
	public String deAccent(String str) {
		str = str.replace(" ", "_");
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

	public String initUrlDocument(ServletContext context, String path_file) {
		Properties prop = new Properties();
		String result = null;
		try {
			prop.load(new FileInputStream(context.getRealPath("/WEB-INF/classes/config.properties")));
			String rootUrl = prop.getProperty("uploadUrl");
			result = rootUrl + path_file;		
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}
	public String getRootWebsiteUrl(ServletContext context, String addingPath) {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(context.getRealPath("/WEB-INF/classes/config.properties")));
			String rootUrl = prop.getProperty("websiteUrl");
			return rootUrl + addingPath;
		} catch (IOException ie) {
			return "";
		}
	}

}
