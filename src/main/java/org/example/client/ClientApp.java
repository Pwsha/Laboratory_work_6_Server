package org.example.client;

public class ClientApp {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        Client client = new Client(host, port);
        ConsoleReader consoleReader = new ConsoleReader(client);
        consoleReader.start();
    }
}