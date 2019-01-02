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
import com.we_learn.dao.QADao;
import com.we_learn.dao.QADaoImpl;

@Path("/qa")
public class QAController extends VerifyToken{
	public QAController(@HeaderParam("Authorization") String token) {
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
		QADao qaDao = (QADaoImpl) this.appContext.getBean("qaDao");
		JSONObject result = qaDao.insert(param, this.userId);
		return Response.status(200).entity(result.toString()).build();
	}
	@POST
	@Path("update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		QADao qaDao = (QADaoImpl) this.appContext.getBean("qaDao");
		JSONObject result = qaDao.update(param, this.userId);
		return Response.status(200).entity(result.toString()).build();
	}
	@PUT
	@Path("remove")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response remove(@HeaderParam("Authorization") String token, String qa) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		QADao qaDao = (QADaoImpl) this.appContext.getBean("qaDao");
		JSONObject result = qaDao.remove(qa, Integer.parseInt(this.userId));
		return Response.status(200).entity(result.toString()).build();
	}

	@PUT
	@Path("restore")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response restore(@HeaderParam("Authorization") String token, String qa) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		QADao qaDao = (QADaoImpl) this.appContext.getBean("qaDao");
		JSONObject result = qaDao.restore(qa, Integer.parseInt(this.userId));
		return Response.status(200).entity(result.toString()).build();
	}
	
	@DELETE
	@Path("delete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@HeaderParam("Authorization") String token, String qa) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		QADao qaDao = (QADaoImpl) this.appContext.getBean("qaDao");
		JSONObject result = qaDao.delete(qa, Integer.parseInt(this.userId));
		return Response.status(200).entity(result.toString()).build();
	}
	
	@POST
	@Path("get-qa-by-page")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQAByPage(@HeaderParam("Authorization") String token, String param) {
//		if (!this.isLogined)
//			return Response.status(200).entity(this.notFoundUser().toString()).build();
		QADao qaDao = (QADaoImpl) this.appContext.getBean("qaDao");
		JSONObject result = qaDao.getQAByPage(param);
		return Response.status(200).entity(result.toString()).build();
	}
	
	@GET
	@Path("get-qa-by-id")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQAById(@HeaderParam("Authorization") String token, @Context HttpServletRequest request) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		QADao qaDao = (QADaoImpl) this.appContext.getBean("qaDao");
		JSONObject result = qaDao.getQAById(request.getParameter("qa_id"));
		return Response.status(200).entity(result.toString()).build();
	}
	
	@GET
	@Path("view-qa-by-id")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response viewQAById(@HeaderParam("Authorization") String token, @Context HttpServletRequest request) {
		QADao qaDao = (QADaoImpl) this.appContext.getBean("qaDao");
		JSONObject result = qaDao.viewQAById(request.getParameter("qa_id"));
		return Response.status(200).entity(result.toString()).build();
	}
}
