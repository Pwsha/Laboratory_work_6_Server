package org.example.client.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.client.Client;

public class MainApp extends Application {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    @Override
    public void start(Stage primaryStage) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        Parameters params = getParameters();
        if (params.getRaw().size() >= 2) {
            host = params.getRaw().get(0);
            port = Integer.parseInt(params.getRaw().get(1));
        }
        Client client = new Client(host, port);
        new AuthController(primaryStage, client);
    }

    public static void main(String[] args) { launch(args); }
}