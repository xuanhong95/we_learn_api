package com.we_learn.domain;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.we_learn.common.MainUtility;
import com.we_learn.common.VerifyToken;
import com.we_learn.dao.UserDao;
import com.we_learn.dao.UserDaoImpl;

@Path("/user")
public class UserController extends VerifyToken{
	public UserController(@HeaderParam("Authorization") String token) {
		super(token);
		// TODO Auto-generated constructor stub
	}
	@Context
	private ServletContext context;
	// private WebApplicationContext appContext = null;
	@Autowired
	private WebApplicationContext appContext = ContextLoader.getCurrentWebApplicationContext();

	@POST
	@Path("get-user-by-page")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserByPage(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		UserDao userDao = (UserDaoImpl) this.appContext.getBean("userDao");
		JSONObject result = userDao.getUserByPage(param);
		return Response.status(200).entity(result.toString()).build();
	}
	
	@PUT
	@Path("active-premium")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response activePremium(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		UserDao userDao = (UserDaoImpl) this.appContext.getBean("userDao");
		JSONObject result = userDao.activePremium(param);
		return Response.status(200).entity(result.toString()).build();
	}
	
	@PUT
	@Path("update-password")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUserPassword(@HeaderParam("Authorization") String token, String user) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		// this.appContext =
		// WebApplicationContextUtils.getWebApplicationContext(context);
		UserDao userDao = (UserDaoImpl) this.appContext.getBean("userDao");

		JSONObject result = userDao.updateUserPassword(user, Integer.parseInt(this.userId));
		return Response.status(200).entity(result.toString()).build();
	}
}
