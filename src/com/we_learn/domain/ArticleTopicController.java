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
import com.we_learn.dao.ArticleDao;
import com.we_learn.dao.ArticleDaoImp;
import com.we_learn.dao.ArticleTopicDao;
import com.we_learn.dao.ArticleTopicDaoImp;

@Path("/article-topic")
public class ArticleTopicController extends VerifyToken{
	public ArticleTopicController(@HeaderParam("Authorization") String token) {
		super(token);
		// TODO Auto-generated constructor stub
	}
	@Context
	private ServletContext context;
	// private WebApplicationContext appContext = null;
	@Autowired
	private WebApplicationContext appContext = ContextLoader.getCurrentWebApplicationContext();
	
	@POST
	@Path("/insert")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response insert(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		ArticleTopicDao articleTopicDao = (ArticleTopicDaoImp) this.appContext.getBean("articleTopicDao");
		JSONObject result = articleTopicDao.insert(param, this.userId);
		return Response.status(200).entity(result.toString()).build();
	}
	
	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		ArticleTopicDao articleTopicDao = (ArticleTopicDaoImp) this.appContext.getBean("articleTopicDao");
		JSONObject result = articleTopicDao.update(param, this.userId);
		return Response.status(200).entity(result.toString()).build();
	}
	@POST
	@Path("get-topic-by-page")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTopicByPage(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		ArticleTopicDao articleTopicDao = (ArticleTopicDaoImp) this.appContext.getBean("articleTopicDao");
		JSONObject result = articleTopicDao.getTopicByPage(param);
		return Response.status(200).entity(result.toString()).build();
	}

	@GET
	@Path("get-topic-by-id")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getArticleById(@HeaderParam("Authorization") String token, @Context HttpServletRequest request) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		ArticleTopicDao articleTopicDao = (ArticleTopicDaoImp) this.appContext.getBean("articleTopicDao");
		JSONObject result = articleTopicDao.getTopicById(request.getParameter("at_id"));
		return Response.status(200).entity(result.toString()).build();
	}
	
	@GET
	@Path("get-all-list-article-topic")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllListArticleTopic(@HeaderParam("Authorization") String token, @Context HttpServletRequest request) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		ArticleTopicDao articleTopicDao = (ArticleTopicDaoImp) this.appContext.getBean("articleTopicDao");
		JSONObject result = articleTopicDao.getAllListArticleTopic();
		return Response.status(200).entity(result.toString()).build();
	}
}
