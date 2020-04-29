package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.*;

import com.google.sps.models.InterviewRequest;

@WebServlet("/alert")
public class AlertServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()){
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }

    String key = getOpenRequest();
    if(key != null) {
        response.setContentType("text/html");
        String number = "1";
        response.getWriter().write(number);
    }
  }

  // Function that returns the key of the open listing that the user has made.
  // If there are no open listings, then returns null.
  public static String getOpenRequest() {
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail = userService.getCurrentUser().getEmail();
    Filter userFilter = new FilterPredicate("username", FilterOperator.EQUAL, userEmail);
    Query q = new Query("InterviewRequest").setFilter(userFilter); 
    PreparedQuery pq = datastore.prepare(q);
    for (Entity result : pq.asIterable()) {
      List<String> times = (List<String>)result.getProperty("timesAvailable");
      boolean closed = (boolean)result.getProperty("closed");
      if (InterviewRequest.checkForOpenTime(times) && !closed)
        return KeyFactory.keyToString(result.getKey());
    }
    return null;
  }
 
}