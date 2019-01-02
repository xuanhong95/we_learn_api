package com.we_learn.domain;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.we_learn.common.VerifyToken;
import com.we_learn.dao.PermissionDao;
import com.we_learn.dao.PermissionDaoImpl;

@Path("/permission")
public class PermissionController extends VerifyToken {

	public PermissionController(@HeaderParam("Authorization") String token) {
		super(token);
		// TODO Auto-generated constructor stub
	}
//	@Context
//	private ServletContext context;
//	private WebApplicationContext appContext = null;
	@Autowired
	private WebApplicationContext appContext = ContextLoader.getCurrentWebApplicationContext();
	
	@GET
	@Path("get-all-permission-tree")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllPermissionSelect(@HeaderParam("Authorization") String token, @Context HttpServletRequest request) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		// this.appContext = WebApplicationContextUtils.getWebApplicationContext(context);
		PermissionDao permissionDao = (PermissionDaoImpl) this.appContext.getBean("permissionDao");
		JSONObject result = permissionDao.getAllPermissionTree();
		return Response.status(200).entity(result.toString()).build();
	}
	
	@GET
	@Path("get-permission-tree-by-group")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPermissionTreeByGroup(@HeaderParam("Authorization") String token, @Context HttpServletRequest request) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		// this.appContext = WebApplicationContextUtils.getWebApplicationContext(context);
		PermissionDao permissionDao = (PermissionDaoImpl) this.appContext.getBean("permissionDao");
		JSONObject result = permissionDao.getPermissionTreeByGroupId(request.getParameter("group_id"));
		return Response.status(200).entity(result.toString()).build();
	}
	@PUT
	@Path("update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updatePermission(@HeaderParam("Authorization") String token, String per) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		// this.appContext = WebApplicationContextUtils.getWebApplicationContext(context);
		PermissionDao permissionDao = (PermissionDaoImpl) this.appContext.getBean("permissionDao");

		JSONObject result = permissionDao.updatePermission(per, Integer.parseInt(this.userId), this.userLogin);
		return Response.status(200).entity(result.toString()).build();
	}

	@GET
	@Path("get-all-group-select")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllGroupSelect(@HeaderParam("Authorization") String token, @Context HttpServletRequest request) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		// this.appContext = WebApplicationContextUtils.getWebApplicationContext(context);
		PermissionDao permissionDao = (PermissionDaoImpl) this.appContext.getBean("permissionDao");
		JSONObject result = permissionDao.getAllGroupSelect();
		return Response.status(200).entity(result.toString()).build();
	}
}
