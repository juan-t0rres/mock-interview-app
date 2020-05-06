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

    if(checkOpenRequest()) {
        response.setContentType("text/html");
        String number = "1";
        response.getWriter().write(number);
    }
  }

  public static boolean checkOpenRequest() {
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail = userService.getCurrentUser().getEmail();

    Filter userFilter = new FilterPredicate("username", FilterOperator.EQUAL, userEmail);
    Query q = new Query("InterviewRequest").setFilter(userFilter); 
    PreparedQuery pq = datastore.prepare(q);
    for (Entity result : pq.asIterable()) {
      List<String> times = (List<String>)result.getProperty("timesAvailable");
      int chosenTime = ((Long)result.getProperty("chosenTime")).intValue();
      boolean closed = (boolean)result.getProperty("closed");
      if (!closed && InterviewRequest.checkForOpenTime(times,chosenTime))
        return true;
    }
    return false;
  }
  
}