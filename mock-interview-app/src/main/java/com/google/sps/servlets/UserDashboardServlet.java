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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.sps.models.InterviewRequest;

@WebServlet("/user")
public class UserDashboardServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService(); 
    if (!userService.isUserLoggedIn()){
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }

    String userEmail = userService.getCurrentUser().getEmail();
    Filter userFilter = new FilterPredicate("username", FilterOperator.EQUAL, userEmail);
	  Query query = new Query("InterviewRequest").setFilter(userFilter).addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    
    List<InterviewRequest> pending = new ArrayList<>();
    List<InterviewRequest> past = new ArrayList<>();
    
    Set<String> keys = getPendingRequests();

    for (String key: keys) {
      pending.add(DataServlet.specificEntity(datastore,key,userEmail).interviewRequest);
    }

    for (Entity entity : results.asIterable()) {
        String entityKey = KeyFactory.keyToString(entity.getKey());
        // If we have used this key before for pending, don't put it in past.
        if (keys.contains(entityKey))
        	continue;
        past.add(DataServlet.specificEntity(datastore,entityKey,userEmail).interviewRequest);
    }

    Filter matchFilter = new FilterPredicate("match", FilterOperator.EQUAL, userEmail);
	  query = new Query("InterviewRequest").setFilter(matchFilter).addSort("timestamp", SortDirection.DESCENDING);
    results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      String entityKey = KeyFactory.keyToString(entity.getKey());
      if (keys.contains(entityKey))
        continue;
      past.add(DataServlet.specificEntity(datastore,entityKey,userEmail).interviewRequest);
  }
    
    response.setContentType("application/json;");
    response.getWriter().println((new Gson()).toJson(new UserDashboard(userEmail,pending,past)));
  }

   // Function that returns the keys of the pending interviews a user has.
  public static Set<String> getPendingRequests() {
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail = userService.getCurrentUser().getEmail();

    Set<String> requests = new HashSet<>();

    Filter userFilter = new FilterPredicate("username", FilterOperator.EQUAL, userEmail);
    Query q = new Query("InterviewRequest").setFilter(userFilter); 
    PreparedQuery pq = datastore.prepare(q);
    for (Entity result : pq.asIterable()) {
      List<String> times = (List<String>)result.getProperty("timesAvailable");
      int chosenTime = ((Long)result.getProperty("chosenTime")).intValue();
      boolean closed = (boolean)result.getProperty("closed");
      if (!closed && InterviewRequest.checkForOpenTime(times,chosenTime))
        requests.add(KeyFactory.keyToString(result.getKey()));
    }
    Filter matchFilter = new FilterPredicate("match", FilterOperator.EQUAL, userEmail);
    q = new Query("InterviewRequest").setFilter(matchFilter);
    pq = datastore.prepare(q);
    for (Entity result : pq.asIterable()) {
      List<String> times = (List<String>)result.getProperty("timesAvailable");
      int chosenTime = ((Long)result.getProperty("chosenTime")).intValue();
      boolean closed = (boolean)result.getProperty("closed");
      if (!closed && InterviewRequest.checkForOpenTime(times,chosenTime))
        requests.add(KeyFactory.keyToString(result.getKey()));
    }
    return requests;
  }

}

class UserDashboard {
	String username;
	List<InterviewRequest> pending;
	List<InterviewRequest> past;

	public UserDashboard(String username, List<InterviewRequest> pending, List<InterviewRequest> past) {
		this.username = username;
		this.pending = pending;
		this.past = past;
	}
}


