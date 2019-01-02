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
import com.we_learn.dao.CreateTestDao;
import com.we_learn.dao.CreateTestDaoImpl;
import com.we_learn.dao.TestDao;
import com.we_learn.dao.TestDaoImp;

@Path("/test")
public class TestController extends VerifyToken{
	public TestController(@HeaderParam("Authorization") String token) {
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
		TestDao testDao = (TestDaoImp) this.appContext.getBean("testDao");
		JSONObject result = testDao.insert(param, this.userId);
		return Response.status(200).entity(result.toString()).build();
	}
	@POST
	@Path("update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		TestDao testDao = (TestDaoImp) this.appContext.getBean("testDao");
		JSONObject result = testDao.update(param, this.userId);
		return Response.status(200).entity(result.toString()).build();
	}
	
	@POST
	@Path("get-list-test")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getListTest(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		TestDao testDao = (TestDaoImp) this.appContext.getBean("testDao");
		JSONObject result = testDao.getAll(this.userId);
		return Response.status(200).entity(result.toString()).build();
	}
	
	@GET
	@Path("get-test-by-id")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTestById(@HeaderParam("Authorization") String token, @Context HttpServletRequest request) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		TestDao testDao = (TestDaoImp) this.appContext.getBean("testDao");
		JSONObject result = testDao.getById(request.getParameter("test_id"));
		return Response.status(200).entity(result.toString()).build();
	}
	@POST
	@Path("get-correct-anwser-by-id")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrectAnswerById(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		TestDao testDao = (TestDaoImp) this.appContext.getBean("testDao");
		JSONObject result = testDao.getCorrectAnswerById(param);
		return Response.status(200).entity(result.toString()).build();
	}
}
