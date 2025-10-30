package com.example.websocket_demo.client;

import javax.swing.*;
import java.util.concurrent.ExecutionException;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui;
            try {
                gui = new ClientGUI();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            gui.promptForUsernameAndConnect();
            gui.setVisible(true);
        });
    }
}
