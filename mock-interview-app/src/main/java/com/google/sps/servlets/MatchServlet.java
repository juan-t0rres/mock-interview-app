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

@WebServlet("/match")
public class MatchServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }

    String key = request.getParameter("key");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key interviewKey = KeyFactory.stringToKey(key);
    Entity entity;
    try {
        entity = datastore.get(interviewKey);   
    }
    catch(Exception e) {
        System.err.println("Error: Cannot find interview listing with given key " + key);
        response.sendRedirect("/?matched=false");
        return;
    }

    entity.setProperty("matched",true);
    datastore.put(entity);

    // We can use these 3 for the email system.
    // We also have the InterviewRequest entity so we can include all other relevant
    // information into the email such as the links, programming language, etc.
    String interviewUser = userService.getCurrentUser().getEmail();
    String listingUser = (String)entity.getProperty("username");
    String time = request.getParameter("time-checkbox");
    
    response.sendRedirect("/?matched=true");
  }
 
}