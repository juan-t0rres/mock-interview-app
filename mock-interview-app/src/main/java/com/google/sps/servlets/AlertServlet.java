package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.appengine.api.datastore.KeyFactory;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.*;

@WebServlet("/alert")
public class AlertServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()){
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }

    if(checkOpenRequest()) {
        response.setContentType("text/html");
        String number = "1";
        response.getWriter().write(number);
    }
  }

  // Function that returns whether or not the current user has a request with an available time (not in the past).
  public static boolean checkOpenRequest() {
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail = userService.getCurrentUser().getEmail();
    Query q = new Query("InterviewRequest"); 
    PreparedQuery pq = datastore.prepare(q);
    for (Entity result : pq.asIterable())
    { 
      if(userEmail.equals(result.getProperty("username"))) {  
        List<String> times = (List<String>)result.getProperty("timesAvailable");
        if(DataServlet.checkForOpenTime(times))
          return true;
      }
    }
    return false;
  }
 
}