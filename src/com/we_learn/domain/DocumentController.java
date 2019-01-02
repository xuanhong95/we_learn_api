package com.we_learn.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.we_learn.common.MainUtility;
import com.we_learn.common.VerifyToken;
import com.we_learn.dao.ArticleTopicContentDao;
import com.we_learn.dao.ArticleTopicContentDaoImp;
import com.we_learn.dao.DocumentDao;
import com.we_learn.dao.DocumentDaoImpl;

@Path("/document")
public class DocumentController extends VerifyToken {
	public DocumentController(@HeaderParam("Authorization") String token) {
		super(token);
		// TODO Auto-generated constructor stub
	}

	private final String DOC_PATH = File.separator + "document" + File.separator;
	@Context
	private ServletContext context;
	// private WebApplicationContext appContext = null;
	@Autowired
	private WebApplicationContext appContext = ContextLoader.getCurrentWebApplicationContext();

	@POST
	@Path("insert")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response insert(@HeaderParam("Authorization") String token,
			@FormDataParam("attachment") List<FormDataBodyPart> lstFile) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		JSONObject result = new JSONObject();
		DocumentDao doc = (DocumentDaoImpl) this.appContext.getBean("documentDao");
		MainUtility mainUtil = new MainUtility();
		Map<String, Object> fileObj = new HashMap<String, Object>();
		for (FormDataBodyPart file : lstFile) {
			String fileName = mainUtil.saveFile(context, DOC_PATH, file);
			if (fileName != null && !fileName.isEmpty()) {
				fileObj.put("file_name", fileName);
				fileObj.put("file_path", DOC_PATH + fileName);
				result = doc.insert(fileObj, this.userId);
				// if (Boolean.parseBoolean(result.get("success").toString())) {
				// result.put("url_document", mainUtil.initUrlDocument(context,
				// fileObj.get("file_path").toString()));
				// }
			} else {
				result.put("success", false);
				result.put("msg", "Upload failed!");
			}
		}
		result.put("success", true);
		return Response.status(200).entity(result.toString()).build();
	}

	@POST
	@Path("get-doc-by-page")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocumentByPage(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		DocumentDao doc = (DocumentDaoImpl) this.appContext.getBean("documentDao");
		JSONObject result = null;
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(context.getRealPath("/WEB-INF/classes/config.properties")));
			result = doc.getDocByPage(param, this.userId, this.groupCode, prop.getProperty("uploadUrl"));
		} catch (IOException ie) {
			return Response.status(200).entity(ie.getMessage()).build();
		}
		return Response.status(200).entity(result.toString()).build();
	}
}
