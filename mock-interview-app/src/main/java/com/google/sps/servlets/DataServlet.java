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
    
    List<InterviewRequest> interviews = new ArrayList<>();

    for (Entity entity : results.asIterable()) {
        String topic = (String)entity.getProperty("topic");
        String spokenLanguage = (String)entity.getProperty("spokenLanguage");
        String programmingLanguage = (String)entity.getProperty("programmingLanguage");
        String communicationURL = (String)entity.getProperty("communicationURL");
        String environmentURL = (String)entity.getProperty("environmentURL");
        List<String> timesAvailable = (List<String>)entity.getProperty("timesAvailable");
        String key = KeyFactory.keyToString(entity.getKey());
        long timestamp = (long)entity.getProperty("timestamp");

        interviews.add(new InterviewRequest(topic,spokenLanguage,programmingLanguage,communicationURL,environmentURL,timesAvailable,key,timestamp));
    }

    response.setContentType("application/json;");
    response.getWriter().println(getJson(interviews));
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()){
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail = userService.getCurrentUser().getEmail();
    boolean isfound = false; 
    Query q = new Query("InterviewRequest"); 
    PreparedQuery pq = datastore.prepare(q);
    for (Entity result : pq.asIterable())
    { 
      if(userEmail.equals(result.getProperty("username"))){
        isfound = true;
        break;
      }
    }
    if(!isfound){
        Entity newInterviewRequest = getInterviewEntity(request);
        datastore.put(newInterviewRequest);    
    }
    response.sendRedirect("/interviews.html");
  }

  public Entity getInterviewEntity(HttpServletRequest request) {
    UserService userService = UserServiceFactory.getUserService();
    String topic = request.getParameter("topic");
    String spokenLanguage = request.getParameter("spokenLanguage");
    String programmingLanguage = request.getParameter("programmingLanguage");
    String communicationURL = request.getParameter("communicationURL");
    String environmentURL = request.getParameter("environmentURL");

    String[] times = request.getParameterValues("time_availability");
    List<String> timesAvailable = Arrays.asList(times);
    String username = userService.getCurrentUser().getEmail();
    Entity interviewEntity = new Entity("InterviewRequest");
    interviewEntity.setProperty("topic",topic);
    interviewEntity.setProperty("spokenLanguage",spokenLanguage);
    interviewEntity.setProperty("programmingLanguage",programmingLanguage);
    interviewEntity.setProperty("communicationURL",communicationURL);
    interviewEntity.setProperty("environmentURL",environmentURL);
    interviewEntity.setProperty("timesAvailable",timesAvailable);
    interviewEntity.setProperty("timestamp",System.currentTimeMillis());
    interviewEntity.setProperty("username",username);
    return interviewEntity;
  }

  public String getJson(List<InterviewRequest> interviews) {
    return (new Gson()).toJson(interviews);
  }
}

class InterviewRequest {
    public String topic, spokenLanguage, programmingLanguage;
    // URLs will tentatively be stored as strings until we decide on a better class to use
    public String communicationURL, environmentURL;
    // Times will tentatively be stored as strings until we decide on a better class to use
    public List<String> timesAvailable;
    public String key;
    public long timestamp;

    public InterviewRequest(String topic, String spokenLanguage, String programmingLanguage, String communicationURL, String environmentURL, 
                            List<String> timesAvailable, String key, long timestamp) {
        this.topic = topic;
        this.spokenLanguage = spokenLanguage;
        this.programmingLanguage = programmingLanguage;
        this.communicationURL = communicationURL;     
        this.environmentURL = environmentURL;
        this.timesAvailable = timesAvailable;  
        this.key = key;
        this.timestamp = timestamp; 
    }
}
   
