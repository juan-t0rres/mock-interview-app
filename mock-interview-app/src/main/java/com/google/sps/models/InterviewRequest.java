package com.google.sps.models;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InterviewRequest {
    public String name, username, intro, topic, spokenLanguage, programmingLanguage;
    // URLs will tentatively be stored as strings until we decide on a better class to use
    public String communicationURL, environmentURL;
    // Times will tentatively be stored as strings until we decide on a better class to use
    public List<String> timesAvailable;
    public String key;
    public long timestamp;
    public boolean closed,matched;

    public InterviewRequest(String name, String intro, String topic, String spokenLanguage, String programmingLanguage, String communicationURL, String environmentURL, 
                            List<String> timesAvailable, String key, String username, boolean closed, boolean matched, long timestamp) {
        this.name = name;
        this.intro = intro;
        this.topic = topic;
        this.spokenLanguage = spokenLanguage;
        this.programmingLanguage = programmingLanguage;
        this.communicationURL = communicationURL;     
        this.environmentURL = environmentURL;
        this.timesAvailable = timesAvailable;  
        this.key = key;
        this.username = username;
        this.closed = closed;
        this.matched = matched;
        this.timestamp = timestamp;
    }

    public static boolean checkForOpenTime(List<String> timesAvailable) {
        LocalDate today = LocalDate.now();
        for(String str: timesAvailable) {
            String time = str.replace('T','-');
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");

            LocalDate datetime = LocalDate.parse(time, f);
            if (datetime.isAfter(today))
                return true;
        }
        return false;
    }
}