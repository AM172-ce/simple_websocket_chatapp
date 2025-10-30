package com.example.websocket_demo.client;

import com.example.websocket_demo.Message;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.lang.NonNull;


import javax.swing.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyStompSessionHandler extends StompSessionHandlerAdapter {
    private final String username;
    private final MessageListener messageListener;
    public MyStompSessionHandler(MessageListener messageListener, String username) {
        this.username = username;
        this.messageListener = messageListener;
    }

    @Override
    public void afterConnected(@NonNull StompSession session, @NonNull StompHeaders connectedHeaders) {
        try{
            session.subscribe("/topic/users", new StompFrameHandler() {
                @Override
                @NonNull
                public Type getPayloadType(@NonNull StompHeaders headers) {
                    return ArrayList.class;
                }

                @Override
                public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                    try {
                        if(payload instanceof ArrayList) {
                            @SuppressWarnings("unchecked")
                            ArrayList<String> activeUsers = (ArrayList<String>) payload;
                            messageListener.onActiveUsersUpdated(activeUsers);
                            System.out.println("Received active users: " + activeUsers);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("Client Subscribe to /topic/users");
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Subscription to /topic/users failed");
        }

        try{
            session.subscribe("/topic/messages", new StompFrameHandler() {
                @Override
                @NonNull
                public Type getPayloadType(@NonNull StompHeaders headers) {
                    return Message.class;
                }

                @Override
                public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                    try {
                        if (payload instanceof Message message) {
                            messageListener.onMessageReceive(message);
                            System.out.println("Received message: " + message.getUser() + ": " + message.getMessage());
                        } else {
                            System.out.println("Received unexpected payload type: " + payload.getClass());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("Client Subscribe to /topic/messages");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Subscription to /topic/messages failed");
        }
        try {
            session.subscribe("/topic/request-allMessages", new StompFrameHandler() {
                @Override
                @NonNull
                public Type getPayloadType(@NonNull StompHeaders headers) {
                    return Message[].class;
                }

                @Override
                public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                    try {
                        if (payload instanceof Message[] messages) {
                            List<Message> currentChatMessages = Arrays.asList(messages);
                            messageListener.onNewUserAdded(new ArrayList<>(currentChatMessages));

                            List<String> currentMtoDebug = currentChatMessages.stream()
                                    .map(Message::toString)
                                    .toList();

                            System.out.println("Current Messages: " + currentMtoDebug);
                        } else {
                            System.out.println("Unexpected payload type: " + payload.getClass());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            System.out.println("Subscription to /topic/request-allMessages failed");
            e.printStackTrace();

        }

        System.out.println("Client Connected");
        session.send("/app/connect", username);

        try{
            session.subscribe("/user/queue/errors", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return String.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    if (payload instanceof String errorMsg) {
                        System.err.println("Error from server: " + errorMsg);

                        // Show error to user
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                                null,
                                errorMsg,
                                "Connection Error",
                                JOptionPane.ERROR_MESSAGE
                        ));

                        // Disconnect the session
                        session.disconnect();

                        // Re-prompt the user
                        SwingUtilities.invokeLater(() -> {
                            try {
                                ClientGUI newClient = new ClientGUI();
                                newClient.promptForUsernameAndConnect();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void handleTransportError(@NonNull StompSession session, @NonNull Throwable exception) {
        super.handleTransportError(session, exception);
    }
}
