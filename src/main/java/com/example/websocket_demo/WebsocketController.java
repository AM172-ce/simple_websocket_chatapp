package com.example.websocket_demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketController {
    private final WebsocketSessionManager sessionManager;
    @Autowired
    public WebsocketController(WebsocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @MessageMapping("/message")
    public void handleMessage(Message message) {
        sessionManager.addMessages(message);
    }

    @MessageMapping("/connect")
    public void connectUser(@Payload String username) {
        if (sessionManager.isUsernameTaken(username)) {
            System.out.println("Rejected duplicate username: " + username);
            return;
        }
        sessionManager.addUsername(username);
        sessionManager.broadcastActiveUsernames();
        sessionManager.broadcastCurrentMessages();
        System.out.println(username + " connected");
    }

    @MessageMapping("/disconnect")
    public void disconnectUser(String username) {
        sessionManager.removeUsername(username);
        sessionManager.broadcastActiveUsernames();
        System.out.println(username + " disconnected");
    }

}
