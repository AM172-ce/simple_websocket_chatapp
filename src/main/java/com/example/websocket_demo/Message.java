package com.example.websocket_demo;

import java.util.UUID;

public class Message {
    private String id;
    private String user;
    private String message;

    public Message(){}
    public Message(String user, String message){
        this.id = UUID.randomUUID().toString();
        this.user = user;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getUser() {
        return user;
    }
    @Override
    public String toString() {
        return "Message{id='" + id + "', user='" + user + "', message='" + message + "'}";
    }
}
