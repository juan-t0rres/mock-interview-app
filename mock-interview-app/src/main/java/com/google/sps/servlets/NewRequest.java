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
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
@WebServlet("/newRequest")
public class NewRequest extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()){
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }
    String userEmail = userService.getCurrentUser().getEmail();
    Filter propertyFilter = new FilterPredicate("username", FilterOperator.EQUAL, userEmail);
    Query q = new Query("InterviewRequest").setFilter(propertyFilter);
    System.out.println(q);
    boolean val = (q == null); 
    response.getWriter().println(val);
     response.getWriter().println(q);
    /*if(q == null){
        System.out.println("Username not found");
        response.sendRedirect("/InterviewRequestForm.html");
    }else{
        response.sendRedirect("/interviews.html");
    }*/
  }
}
      
