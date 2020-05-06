package com.google.sps.models;

import java.util.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class InterviewRequest {
    public String name, username, intro, topic, spokenLanguage, programmingLanguage, match;
    // URLs will tentatively be stored as strings until we decide on a better class to use
    public String communicationURL, environmentURL;
    // Times will tentatively be stored as strings until we decide on a better class to use
    public List<String> timesAvailable;
    public String key;
    public long timestamp;
    public boolean closed;
    public int chosenTime;

    public InterviewRequest(String name, String intro, String topic, String spokenLanguage, String programmingLanguage, String communicationURL, String environmentURL, 
                            List<String> timesAvailable, String key, String username, boolean closed, String match, int chosenTime, long timestamp) {
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
        this.match = match;
        this.chosenTime = chosenTime;
        this.timestamp = timestamp;
    }

    public static boolean checkForOpenTime(List<String> timesAvailable, int chosenTime) {
        LocalDateTime today = LocalDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");
        if (chosenTime != -1) {
            String time = timesAvailable.get(chosenTime).replace('T','-');
            LocalDateTime datetime = LocalDateTime.parse(time,f);
            return today.isBefore(datetime);
        }
        for(String str: timesAvailable) {
            String time = str.replace('T','-');
            LocalDateTime datetime = LocalDateTime.parse(time, f);
            if (today.isBefore(datetime))
                return true;
        }
        return false;
    }
}