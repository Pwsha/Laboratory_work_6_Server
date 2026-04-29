package org.example.client;

import io.github.cdimascio.dotenv.Dotenv;

public class ClientApp {
    private static String host;
    private static int port;

    static {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();

            host = dotenv.get("SERVER_HOST", "localhost");
            String portStr = dotenv.get("SERVER_PORT", "8080");
            port = Integer.parseInt(portStr);

        } catch (Exception e) {
            host = "localhost";
            port = 8080;
        }
    }

    public static void main(String[] args) {
        String finalHost = args.length > 0 ? args[0] : host;
        int finalPort = args.length > 1 ? Integer.parseInt(args[1]) : port;

        Client client = new Client(finalHost, finalPort);
        ConsoleReader consoleReader = new ConsoleReader(client);
        consoleReader.start();
    }
}