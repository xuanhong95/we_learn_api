package com.we_learn.domain;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.we_learn.common.VerifyToken;
import com.we_learn.dao.QACommentDao;
import com.we_learn.dao.QACommentDaoImp;
import com.we_learn.dao.WrittingTestDao;
import com.we_learn.dao.WrittingTestDaoImp;
import com.we_learn.dao.WrittingTestTopicDao;
import com.we_learn.dao.WrittingTestTopicDaoImp;

@Path("/writing-test-topic")
public class WritingTestTopicController extends VerifyToken {

	public WritingTestTopicController(@HeaderParam("Authorization") String token) {
		super(token);
		// TODO Auto-generated constructor stub
	}

	@Context
	private ServletContext context;
	// private WebApplicationContext appContext = null;
	@Autowired
	private WebApplicationContext appContext = ContextLoader.getCurrentWebApplicationContext();

	@POST
	@Path("insert")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response insert(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		WrittingTestTopicDao writtingTestTopicDao = (WrittingTestTopicDaoImp) this.appContext
				.getBean("writingTestTopicDao");
		JSONObject result = writtingTestTopicDao.insert(param, this.userId);
		return Response.status(200).entity(result.toString()).build();
	}

	@GET
	@Path("get-wtt-by-id")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWtById(@HeaderParam("Authorization") String token, @Context HttpServletRequest request) {
		WrittingTestTopicDao writingTestTopicDao = (WrittingTestTopicDaoImp) this.appContext
				.getBean("writingTestTopicDao");
		JSONObject result = writingTestTopicDao.getById(request.getParameter("wtt_id"));
		return Response.status(200).entity(result.toString()).build();
	}

	@POST
	@Path("get-wtt-by-page")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWttByPage(@HeaderParam("Authorization") String token, String param) {
		// if (!this.isLogined)
		// return Response.status(200).entity(this.notFoundUser().toString()).build();
		WrittingTestTopicDao writingTestTopicDao = (WrittingTestTopicDaoImp) this.appContext
				.getBean("writingTestTopicDao");
		JSONObject result = writingTestTopicDao.getTopicByPage(param);
		return Response.status(200).entity(result.toString()).build();
	}

	@POST
	@Path("insert-comment")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response insertComment(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		WrittingTestTopicDao writingTestTopicDao = (WrittingTestTopicDaoImp) this.appContext
				.getBean("writingTestTopicDao");
		JSONObject result = writingTestTopicDao.insertComment(param, this.userId, this.groupCode);
		return Response.status(200).entity(result.toString()).build();
	}

	@POST
	@Path("update-comment")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateComment(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		WrittingTestTopicDao writingTestTopicDao = (WrittingTestTopicDaoImp) this.appContext
				.getBean("writingTestTopicDao");
		JSONObject result = writingTestTopicDao.updateComment(param, this.userId);
		return Response.status(200).entity(result.toString()).build();
	}
}
