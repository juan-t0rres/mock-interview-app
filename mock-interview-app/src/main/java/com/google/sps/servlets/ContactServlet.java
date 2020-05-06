package com.google.sps.servlets;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;
import com.google.appengine.api.mail.BounceNotification.Details;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.*;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import java.util.*;


@WebServlet("/contact")
public class ContactServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        String email = userService.getCurrentUser().getEmail();
        String name = request.getParameter("name");
        String topic = request.getParameter("topic");
        String message = request.getParameter("message");
        
        response.getWriter().print("Sending email with contact info.");
        sendSimpleMail(email, name, topic, message);
        response.sendRedirect("/");
    }

    private void sendSimpleMail(String email, String name, String topic, String message) {
    // [START simple_example]
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    try {
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress("contact@match-mocker.appspotmail.com", "Match Mocker Contact Form"));
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress("matchmocker@gmail.com", "Match Mocker Team"));
      msg.setSubject("Contact Message From Match Mocker User");
      msg.setText("Message from " + name + "\n \n" + 
      "* Topic: " + topic + "\n \n" +
      "* Message: " + message + "\n \n" + 
      "* Reply-Email: " + email);
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



}