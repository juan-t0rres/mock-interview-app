// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;
import com.google.appengine.api.mail.BounceNotification.Details;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.sps.models.InterviewRequest;


/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService(); 
    if (!userService.isUserLoggedIn()){
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }
    Query query = new Query("InterviewRequest").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    
    String key = request.getParameter("key");
    
    response.setContentType("application/json;");
    if (key != null) {
        // Handle request for specific entity
        String userEmail = userService.getCurrentUser().getEmail();
        response.getWriter().println(getJson(specificEntity(datastore, key, userEmail)));
    } 
    else {
        // Handle request for all entities
        List<InterviewRequest> interviews = new ArrayList<>();
        allEntities(results, interviews);
        response.getWriter().println(getJson(interviews));
    }
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()){
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity newInterviewRequest = getInterviewEntity(request);
    datastore.put(newInterviewRequest); 
    String key = KeyFactory.keyToString(newInterviewRequest.getKey());
    response.sendRedirect("/InterviewRequestDetails.html?key=" + key);
  }

  public void allEntities(PreparedQuery results, List<InterviewRequest> interviews) {
    for (Entity entity : results.asIterable()) {
        List<String> timesAvailable = (List<String>)entity.getProperty("timesAvailable");
        boolean closed = (boolean)entity.getProperty("closed");
        String match = (String)entity.getProperty("match");
        int chosenTime = ((Long)entity.getProperty("chosenTime")).intValue();
        if(closed || match != null || !InterviewRequest.checkForOpenTime(timesAvailable,chosenTime))
            continue;

        String name = (String)entity.getProperty("name");
        String intro = (String)entity.getProperty("intro");
        String topic = (String)entity.getProperty("topic");
        String spokenLanguage = (String)entity.getProperty("spokenLanguage");
        String programmingLanguage = (String)entity.getProperty("programmingLanguage");
        String communicationURL = (String)entity.getProperty("communicationURL");
        String environmentURL = (String)entity.getProperty("environmentURL");
        String key = KeyFactory.keyToString(entity.getKey());
        String username = (String)entity.getProperty("username");
        long timestamp = (long)entity.getProperty("timestamp");

        interviews.add(new InterviewRequest(name,intro,topic,spokenLanguage,programmingLanguage,
        communicationURL,environmentURL,timesAvailable,key,username,closed,match,chosenTime,timestamp));
    }
  }

  public static DetailsResponse specificEntity(DatastoreService datastore, String key, String userEmail) {
    Key interviewKey = KeyFactory.stringToKey(key);
    Entity entity;
    try {
        entity = datastore.get(interviewKey);   
    }
    catch(Exception e) {
        System.err.println("Error: Cannot find interview listing with given key " + key);
        return null;
    }
    String name = (String)entity.getProperty("name");
    String intro = (String)entity.getProperty("intro");
    String topic = (String)entity.getProperty("topic");
    String spokenLanguage = (String)entity.getProperty("spokenLanguage");
    String programmingLanguage = (String)entity.getProperty("programmingLanguage");
    String communicationURL = (String)entity.getProperty("communicationURL");
    String environmentURL = (String)entity.getProperty("environmentURL");
    List<String> timesAvailable = (List<String>)entity.getProperty("timesAvailable");
    String username = (String)entity.getProperty("username");
    boolean closed = (boolean)entity.getProperty("closed");
    String match = (String)entity.getProperty("match");
    int chosenTime = ((Long)entity.getProperty("chosenTime")).intValue();
    long timestamp = (long)entity.getProperty("timestamp");

    boolean hideForm = userEmail.equals(username) || closed || (match != null);

    InterviewRequest interviewRequest = new InterviewRequest(name,intro,topic,spokenLanguage,programmingLanguage,
    communicationURL,environmentURL,timesAvailable,key,username,closed,match,chosenTime,timestamp);

    return new DetailsResponse(interviewRequest,hideForm,match!=null);
  }

  public Entity getInterviewEntity(HttpServletRequest request) {
    UserService userService = UserServiceFactory.getUserService();
    String name = request.getParameter("name");
    String intro = request.getParameter("intro");
    String topic = request.getParameter("topic");
    String spokenLanguage = request.getParameter("spokenLanguage");
    String programmingLanguage = request.getParameter("programmingLanguage");
    String communicationURL = request.getParameter("communicationURL");
    String environmentURL = request.getParameter("environmentURL");

    String[] times = request.getParameterValues("time_availability");
    List<String> timesAvailable = Arrays.asList(times);
    //backend check for maximum time availability entries allowed (10)
    if(timesAvailable.size() > 10) {
        timesAvailable = timesAvailable.subList(0, 11);
    }
    String username = userService.getCurrentUser().getEmail();
    Entity interviewEntity = new Entity("InterviewRequest");
    interviewEntity.setProperty("name",name);
    interviewEntity.setProperty("intro",intro);
    interviewEntity.setProperty("topic",topic);
    interviewEntity.setProperty("spokenLanguage",spokenLanguage);
    interviewEntity.setProperty("programmingLanguage",programmingLanguage);
    interviewEntity.setProperty("communicationURL",communicationURL);
    interviewEntity.setProperty("environmentURL",environmentURL);
    interviewEntity.setProperty("timesAvailable",timesAvailable);
    interviewEntity.setProperty("timestamp",System.currentTimeMillis());
    interviewEntity.setProperty("username",username);
    interviewEntity.setProperty("closed",false);
    interviewEntity.setProperty("match", null);
    interviewEntity.setProperty("chosenTime",-1);
    return interviewEntity;
  }

  public String getJson(List<InterviewRequest> interviews) {
    return (new Gson()).toJson(interviews);
  }

  public String getJson(DetailsResponse detailsResponse) {
    return (new Gson()).toJson(detailsResponse);
  }
}

class DetailsResponse {
  InterviewRequest interviewRequest;
  boolean hideForm, matched;

  public DetailsResponse(InterviewRequest interviewRequest, boolean hideForm, boolean matched) {
    this.interviewRequest = interviewRequest;
    this.hideForm = hideForm;
    this.matched = matched;
  }
}
   
