package com.example.websocket_demo.client;


import com.example.websocket_demo.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ClientGUI extends JFrame  implements MessageListener{
    private JPanel connectedUsersPanel, messagePanel;
    private MyStompClient myStompClient;
    private String username;
    private JScrollPane messagePanelScrollPane;
    private final Set<String> displayedMessageIds = new HashSet<>();


    public ClientGUI() throws ExecutionException, InterruptedException {
        super("Chat Application");

        setSize(1218, 685);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(ClientGUI.this,
                        "Do you really want to leave?", "Exit", JOptionPane.YES_NO_OPTION);
                if(option == JOptionPane.YES_OPTION) {
                    myStompClient.disconnectUser(username);
                    ClientGUI.this.dispose();
                }
            }
        });

        getContentPane().setBackground(Utilities.PRIMARY_COLOR);
        addGuiComponents();
    }

    public void promptForUsernameAndConnect() {
        while (true) {
            String inputUsername = JOptionPane.showInputDialog(
                    this,
                    "Enter Username (Max: 16 Characters)",
                    "Chat Application",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (inputUsername == null) {
                this.dispose();
                return;   // Cancelled
            }

            if (inputUsername.isEmpty() || inputUsername.length() > 16) {
                JOptionPane.showMessageDialog(this,
                        "Invalid username. It must be non-empty and up to 16 characters.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                this.username = inputUsername;
                this.setTitle("User " + username);
                System.out.println(username);
                myStompClient = new MyStompClient(this, username);
                break; // Success
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Connection failed. Try again.",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addGuiComponents() {
        addConnectedUserComponents();
        addChatComponents();
    }
    private void addConnectedUserComponents() {
        connectedUsersPanel = new JPanel();
        connectedUsersPanel.setBorder(Utilities.addPadding(10, 10, 10, 10));
        connectedUsersPanel.setLayout(new BoxLayout(connectedUsersPanel,
                BoxLayout.Y_AXIS));
        connectedUsersPanel.setBackground(Utilities.SECONDARY_COLOR);
        connectedUsersPanel.setPreferredSize(new Dimension(200, getHeight()));

        JLabel connectedUsersLabel = new JLabel("Connected Users");
        connectedUsersLabel.setFont(new Font("Inter", Font.BOLD, 18));
        connectedUsersLabel.setForeground(Utilities.TEXT_COLOR);
        connectedUsersPanel.add(connectedUsersLabel);

        add(connectedUsersPanel, BorderLayout.WEST);
    }

    private void addChatComponents() {
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBackground(Utilities.TRANSPARENT_COLOR);

        messagePanel = new JPanel();
        messagePanel.setBorder(Utilities.addPadding(7, 7, 7, 7));
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Utilities.TRANSPARENT_COLOR);

        messagePanelScrollPane = new JScrollPane(messagePanel);
        messagePanelScrollPane.setBackground(Utilities.TRANSPARENT_COLOR);
        messagePanelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagePanelScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        messagePanelScrollPane.getVerticalScrollBar().setBackground(Utilities.TRANSPARENT_COLOR);
        messagePanelScrollPane.getViewport().addChangeListener(e -> {
            revalidate();
            repaint();
        });
        chatPanel.add(messagePanelScrollPane, BorderLayout.CENTER);


        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(Utilities.addPadding(7, 7, 7, 7));
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBackground(Utilities.TRANSPARENT_COLOR);

        JTextField inputField = getJTextField(inputPanel);
        inputPanel.add(inputField, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);


        add(chatPanel, BorderLayout.CENTER);
    }

    private JTextField getJTextField(JPanel inputPanel) {
        JTextField inputField = new JTextField();
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER) {
                    String input = inputField.getText();

                    if(input.isEmpty()) return;

                    inputField.setText("");

                    myStompClient.sendMessage(new Message(username, input));
                }
            }
        });
        inputField.setBackground(Utilities.SECONDARY_COLOR);
        inputField.setForeground(Utilities.TEXT_COLOR);
        inputField.setBorder(Utilities.addPadding(0, 6, 0, 6));
        inputField.setFont(new Font("Inter", Font.BOLD, 16));
        inputField.setPreferredSize(new Dimension(inputPanel.getWidth(), 50));
        return inputField;
    }

    private JPanel createChatMessageComponent(Message message) {
        JPanel chatMessage = new JPanel();
        chatMessage.setBackground(Utilities.TRANSPARENT_COLOR);
        chatMessage.setLayout(new BoxLayout(chatMessage, BoxLayout.Y_AXIS));
        chatMessage.setBorder(Utilities.addPadding(15, 13, 10, 13));

        JLabel usernameLabel = new JLabel(message.getUser());
        usernameLabel.setFont(new Font("Inter", Font.BOLD, 15));
        usernameLabel.setForeground(Utilities.TEXT_COLOR2);
        chatMessage.add(usernameLabel);

         JLabel messageLabel = new JLabel(message.getMessage());
         messageLabel.setFont(new Font("Inter", Font.PLAIN, 18));
         messageLabel.setForeground(Utilities.TEXT_COLOR);
         chatMessage.add(messageLabel);

         return chatMessage;
    }

    @Override
    public void onMessageReceive(Message message) {
        if(!displayedMessageIds.contains(message.getId())) {
            displayedMessageIds.add(message.getId());
            messagePanel.add(createChatMessageComponent(message));
            revalidate();
            repaint();

            messagePanelScrollPane.getVerticalScrollBar().setValue(Integer.MAX_VALUE);
        }
    }

    @Override
    public void onActiveUsersUpdated(ArrayList<String> users) {
       if(connectedUsersPanel.getComponents().length >= 2) {
           connectedUsersPanel.remove(1);
       }
       JPanel userListPanel = new JPanel();
       userListPanel.setBackground(Utilities.TRANSPARENT_COLOR);
       userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));

       for(String user : users) {
           JLabel username = new JLabel();
           username.setText(user);
           username.setForeground(Utilities.TEXT_COLOR2);
           username.setFont(new Font("Inter", Font.BOLD, 16));
           userListPanel.add(username);
       }

       connectedUsersPanel.add(userListPanel);
       revalidate();
       repaint();
    }

    @Override
    public void onNewUserAdded(ArrayList<Message> currentChatMessages) {
        for(Message message : currentChatMessages) {
            onMessageReceive(message);
        }
    }
}
