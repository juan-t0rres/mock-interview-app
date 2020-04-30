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
    
    InterviewRequest pending = null;
    List<InterviewRequest> past = new ArrayList<>();
    
    String key = AlertServlet.getOpenRequest();

    if (key != null)
    	pending = DataServlet.specificEntity(datastore,key);

    for (Entity entity : results.asIterable()) {
        String entityKey = KeyFactory.keyToString(entity.getKey());
        if (entityKey.equals(key))
        	continue;

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
        long timestamp = (long)entity.getProperty("timestamp");

        past.add(new InterviewRequest(name,intro,topic,spokenLanguage,programmingLanguage,communicationURL,environmentURL,timesAvailable,entityKey,username,closed,timestamp));
    }
    
    response.setContentType("application/json;");
    response.getWriter().println((new Gson()).toJson(new UserDashboard(userEmail,pending,past)));
  }

}

class UserDashboard {
	String username;
	InterviewRequest pending;
	List<InterviewRequest> past;

	public UserDashboard(String username, InterviewRequest pending, List<InterviewRequest> past) {
		this.username = username;
		this.pending = pending;
		this.past = past;
	}
}


