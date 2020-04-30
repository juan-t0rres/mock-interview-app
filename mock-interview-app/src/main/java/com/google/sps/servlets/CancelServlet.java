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

@WebServlet("/cancel")
public class CancelServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()){
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
        return;
    }

    String userEmail = userService.getCurrentUser().getEmail();
    String username = (String)entity.getProperty("username");
    
    // Make sure the user making the request is the same user who made the listing.
    if (userEmail.equals(username)) {
      entity.setProperty("closed",true);
      datastore.put(entity);
    }

    response.sendRedirect("/");
  }
 
}