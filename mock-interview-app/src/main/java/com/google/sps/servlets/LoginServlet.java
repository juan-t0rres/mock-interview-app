package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.util.*; 
import com.google.gson.Gson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


  

  @WebServlet("/login")
public class LoginServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    boolean loggedIn = userService.isUserLoggedIn();
    String url = loggedIn ? userService.createLogoutURL("/") : userService.createLoginURL("/redirect");
    LoginResponse lr = new LoginResponse(loggedIn,url);

    response.setContentType("application/json;");
    response.getWriter().println((new Gson()).toJson(lr));
  }
}

class LoginResponse {
  public boolean loggedIn;
  public String url;
  public LoginResponse(boolean loggedIn, String url) {
    this.loggedIn = loggedIn;
    this.url = url;
  }
}