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
// [START simple_includes]
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
// [END simple_includes]
// [START multipart_includes]
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataContentHandler;
import javax.activation.DataHandler;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
// [END multipart_includes]

@WebServlet("/match")
@SuppressWarnings("serial")
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
    
    String type = request.getParameter("type");
    if (type != null && type.equals("multipart")) {
      response.getWriter().print("Sending HTML email with attachment.");
      sendMultipartMail(interviewUser, listingUser);
    } else {
      response.getWriter().print("Sending simple email.");
      sendSimpleMail(interviewUser, listingUser);
    }

    response.sendRedirect("/?matched=true");
  }



  private void sendSimpleMail(String interviewUser, String listingUser) {
    // [START simple_example]
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    try {
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress("interviews@match-mocker.appspotmail.com", "Match Mocker Team"));
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(listingUser, "Match Mocker User"));
      msg.setSubject("Someone has accepted your Mock Interview Request!");
      msg.setText("Congrats!!! Check out your Mock Interview details!");
      Transport.send(msg);
    } catch (AddressException e) {
      // ...
    } catch (MessagingException e) {
      // ...
    } catch (UnsupportedEncodingException e) {
      // ...
    }
    // [END simple_example]
  }

  private void sendMultipartMail(String interviewUser, String listingUser) {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    String msgBody = "...";

    try {
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress("interviews@match-mocker.appspotmail.com", "Match Mocker Team"));
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(listingUser, "Match Mocker User"));
      msg.setSubject("Someone has accepted your Mock Interview Request!");
      msg.setText(msgBody);

      // [START multipart_example]
      String htmlBody = "<h2>Congrats!!! Check out your Mock Interview details! </h2>";     
      byte[] attachmentData = null;  // ...
      Multipart mp = new MimeMultipart();

      MimeBodyPart htmlPart = new MimeBodyPart();
      htmlPart.setContent(htmlBody, "text/html");
      mp.addBodyPart(htmlPart);

      MimeBodyPart attachment = new MimeBodyPart();
      InputStream attachmentDataStream = new ByteArrayInputStream(attachmentData);
      attachment.setFileName("manual.pdf");
      attachment.setContent(attachmentDataStream, "application/pdf");
      mp.addBodyPart(attachment);

      msg.setContent(mp);
      // [END multipart_example]

      Transport.send(msg);

    } catch (AddressException e) {
      // ...
    } catch (MessagingException e) {
      // ...
    } catch (UnsupportedEncodingException e) {
      // ...
    }
  }

 
}