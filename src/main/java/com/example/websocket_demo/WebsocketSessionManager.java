package com.example.websocket_demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class WebsocketSessionManager {
    private final ArrayList<String> activeUsernames = new ArrayList<>();
    private final ArrayList<Message> currentChatMessages = new ArrayList<>();
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    public WebsocketSessionManager(SimpMessagingTemplate messagingTemplate){
        this.messagingTemplate = messagingTemplate;
    }
    public void addUsername(String username) {
        activeUsernames.add(username);
    }
    public boolean isUsernameTaken(String username) {
        messagingTemplate.convertAndSendToUser(username, "/queue/errors", "Username already taken");
        return activeUsernames.contains(username);
    }

    public void removeUsername(String username) {
        activeUsernames.remove(username);
    }
    public void broadcastActiveUsernames() {
        messagingTemplate.convertAndSend("/topic/users", activeUsernames);
        System.out.println("Broadcasting active users to /topic/users" + activeUsernames);
    }
    public void addMessages(Message message) {
        System.out.println("Received message from user: " + message.getUser() + ": " + message.getMessage());
        messagingTemplate.convertAndSend("/topic/messages", message);
        currentChatMessages.add(message);
        System.out.println("Sent message to /topic/messages: " + message.getUser() + ": " + message.getMessage());
    }
    public void broadcastCurrentMessages() {
        messagingTemplate.convertAndSend("/topic/request-allMessages", currentChatMessages);
        ArrayList<String> currentMtoDebug = new ArrayList<>();
        for(Message m : currentChatMessages){
            currentMtoDebug.add(m.toString());
        }
        System.out.println("Current Messages: " + currentMtoDebug);
    }
}
