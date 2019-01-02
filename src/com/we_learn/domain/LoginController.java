package com.we_learn.domain;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
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

import com.we_learn.common.MainUtility;
import com.we_learn.dao.LoginDao;
import com.we_learn.dao.LoginDaoImp;

@Path("/")
public class LoginController {
	@Context
	private ServletContext context;
	@Autowired
	private WebApplicationContext appContext = ContextLoader.getCurrentWebApplicationContext();

	@POST
	@Path("login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(String params) {
		// this.appContext =
		// WebApplicationContextUtils.getWebApplicationContext(context);
		LoginDao authDao = (LoginDaoImp) this.appContext.getBean("loginDao");
		JSONObject result = authDao.login(params);
		String success = result.get("success").toString();
		String Authorization = "";
		if (success.equals("true")) {
			Authorization = result.get("Authorization").toString();
			result.remove("Authorization");
		}
		return Response.status(200).header("Authorization", Authorization).entity(result.toString()).build();
	}

	@POST
	@Path("logout")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response logout(@HeaderParam("Authorization") String token) {
		// this.appContext =
		// WebApplicationContextUtils.getWebApplicationContext(context);
		LoginDao authDao = (LoginDaoImp) this.appContext.getBean("loginDao");
		JSONObject result = authDao.logout(token);
		return Response.status(200).entity(result.toString()).build();
	}

	@POST
	@Path("sign-up")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response signUp(String params) {
		LoginDao authDao = (LoginDaoImp) this.appContext.getBean("loginDao");
		MainUtility mainUtil = new MainUtility();
		JSONObject result = authDao.signUp(params, mainUtil.getRootWebsiteUrl(context, ""));
		return Response.status(200).entity(result.toString()).build();
	}
	@POST
	@Path("active-account")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response activeAccount(String params) {
		LoginDao authDao = (LoginDaoImp) this.appContext.getBean("loginDao");
		JSONObject result = authDao.activeAccount(params);
		return Response.status(200).entity(result.toString()).build();
	}
	@POST
	@Path("resend-active-code")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response resendActiveCode(String params) {
		LoginDao authDao = (LoginDaoImp) this.appContext.getBean("loginDao");
		MainUtility mainUtil = new MainUtility();
		JSONObject result = authDao.resendActiveCode(params, mainUtil.getRootWebsiteUrl(context, ""));
		return Response.status(200).entity(result.toString()).build();
	}
}
