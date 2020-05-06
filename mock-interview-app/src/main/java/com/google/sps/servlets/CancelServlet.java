package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
    String match = (String)entity.getProperty("match");
    // Make sure the user cancelling is the same user who made the listing or matched.
    if (userEmail.equals(username) || userEmail.equals(match)) {

      // If the listing was matched, send email to other user.
      if (match != null) {
        boolean interviewer = userEmail.equals(match);
        String user = interviewer ? username : match;
        int chosenTime = ((Long)entity.getProperty("chosenTime")).intValue();
        String t = ((List<String>)entity.getProperty("timesAvailable")).get(chosenTime).replace('T', '-');

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");
        LocalDateTime date = LocalDateTime.parse(t,f);
        
        String formattedDate = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
        sendSimpleMail(user,interviewer,entity,formattedDate);
      }

      entity.setProperty("closed",true);
      datastore.put(entity);
    }

    response.sendRedirect("/?cancel=true");
  }

  private void sendSimpleMail(String user, boolean interviewer, Entity entity, String time) {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    String name = (String)entity.getProperty("name");
    try {
      Message msg = new MimeMessage(session);
      String replyEmailString = "matchmocker@gmail.com";
      InternetAddress[] replyAddress = InternetAddress.parse(replyEmailString);
      msg.setFrom(new InternetAddress("interviews@match-mocker.appspotmail.com", "Match Mocker Team"));
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(user, name));
      msg.setSubject("Mock Interview Cancelled");
      msg.setReplyTo(replyAddress);
      String text = "Feel free to " + (interviewer ?  "request a new mock interview." : "interview another user.");
      msg.setText("Your mock interview scheduled on " + time + " has been cancelled by the other user.\n" + text);
      Transport.send(msg);
    }
    catch (Exception e) {
      System.err.println("Error sending cancel email:" + e);
    }
  }
}