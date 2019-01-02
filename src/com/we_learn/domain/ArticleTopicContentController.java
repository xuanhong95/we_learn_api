package com.we_learn.domain;

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

import com.we_learn.common.VerifyToken;
import com.we_learn.dao.ArticleDao;
import com.we_learn.dao.ArticleDaoImp;
import com.we_learn.dao.ArticleTopicContentDao;
import com.we_learn.dao.ArticleTopicContentDaoImp;
@Path("/atc")
public class ArticleTopicContentController extends VerifyToken{

	public ArticleTopicContentController(@HeaderParam("Authorization") String token) {
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
		ArticleTopicContentDao articleTopicContentDao = (ArticleTopicContentDaoImp) this.appContext.getBean("articleTopicContentDao");
		JSONObject result = articleTopicContentDao.insert(param, this.userId);
		return Response.status(200).entity(result.toString()).build();
	}
	@POST
	@Path("update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		ArticleTopicContentDao articleTopicContentDao = (ArticleTopicContentDaoImp) this.appContext.getBean("articleTopicContentDao");
		JSONObject result = articleTopicContentDao.update(param, this.userId);
		return Response.status(200).entity(result.toString()).build();
	}
	@PUT
	@Path("remove")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response remove(@HeaderParam("Authorization") String token, String article) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		ArticleTopicContentDao articleTopicContentDao = (ArticleTopicContentDaoImp) this.appContext.getBean("articleTopicContentDao");
		JSONObject result = articleTopicContentDao.remove(article, Integer.parseInt(this.userId));
		return Response.status(200).entity(result.toString()).build();
	}

	@PUT
	@Path("restore")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response restore(@HeaderParam("Authorization") String token, String article) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		ArticleTopicContentDao articleTopicContentDao = (ArticleTopicContentDaoImp) this.appContext.getBean("articleTopicContentDao");
		JSONObject result = articleTopicContentDao.restore(article, Integer.parseInt(this.userId));
		return Response.status(200).entity(result.toString()).build();
	}

	@DELETE
	@Path("delete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@HeaderParam("Authorization") String token, String article) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		ArticleTopicContentDao articleTopicContentDao = (ArticleTopicContentDaoImp) this.appContext.getBean("articleTopicContentDao");
		JSONObject result = articleTopicContentDao.delete(article, Integer.parseInt(this.userId));
		return Response.status(200).entity(result.toString()).build();
	}

	@POST
	@Path("get-article-by-page")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTopicByPage(@HeaderParam("Authorization") String token, String param) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		ArticleTopicContentDao articleTopicContentDao = (ArticleTopicContentDaoImp) this.appContext.getBean("articleTopicContentDao");
		JSONObject result = articleTopicContentDao.getTopicByPage(param);
		return Response.status(200).entity(result.toString()).build();
	}

	@GET
	@Path("get-article-by-id")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getArticleById(@HeaderParam("Authorization") String token, @Context HttpServletRequest request) {
		if (!this.isLogined)
			return Response.status(200).entity(this.notFoundUser().toString()).build();
		ArticleTopicContentDao articleTopicContentDao = (ArticleTopicContentDaoImp) this.appContext.getBean("articleTopicContentDao");
		JSONObject result = articleTopicContentDao.getArticleById(request.getParameter("atc_id"));
		return Response.status(200).entity(result.toString()).build();
	}

	@GET
	@Path("view-article-by-id")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response viewArticleById(@HeaderParam("Authorization") String token, @Context HttpServletRequest request) {
		ArticleTopicContentDao articleTopicContentDao = (ArticleTopicContentDaoImp) this.appContext.getBean("articleTopicContentDao");
		JSONObject result = articleTopicContentDao.viewArticleById(request.getParameter("atc_id"));
		return Response.status(200).entity(result.toString()).build();
	}
//	@POST
//	@Path("get-article-by-type")
//	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response getArticleByType(@HeaderParam("Authorization") String token, String param) {
//		if (!this.isLogined)
//			return Response.status(200).entity(this.notFoundUser().toString()).build();
//		ArticleDao articleDao = (ArticleDaoImp) this.appContext.getBean("articleDao");
//		JSONObject result = articleDao.getListArticleByType(param);
//		return Response.status(200).entity(result.toString()).build();
//	}
}
