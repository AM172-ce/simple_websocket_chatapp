package com.example.websocket_demo.client;

import com.example.websocket_demo.Message;
import org.apache.tomcat.websocket.WsRemoteEndpointAsync;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MyStompClient {
    private StompSession session;
    private final String username;
    private MessageListener messageListener;

    public MyStompClient(MessageListener messageListener, String username) throws ExecutionException, InterruptedException {
        this.username = username;
        this.messageListener = messageListener;
        connect();
    }

    public void connect() throws ExecutionException, InterruptedException {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));

        SockJsClient sockJsClient = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler sessionHandler = new MyStompSessionHandler(messageListener ,username);
        String url = "ws://localhost:8080/ws";

        session = stompClient.connectAsync(url, sessionHandler).get();
    }

    public void sendMessage(Message message){
        try {
            session.send("/app/message", message);
            System.out.println("Message sent: " + message.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StompSession getSession() {
        return session;
    }

    public void disconnectUser(String username) {
        try {
            if (session != null && session.isConnected()) {
                session.send("/app/disconnect", username);
                session.disconnect();
                session = null;
                System.out.println("User disconnected: " + username);
            } else {
                System.out.println("Session already closed for user: " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
