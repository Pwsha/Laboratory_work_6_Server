package org.example.server;

import java.util.Scanner;

public class ServerApp {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        String filename = "server_data/data.csv";

        Scanner scanner = new Scanner(System.in);
        CollectionManager collectionManager = new CollectionManager(filename);

        Server server = new Server(port, collectionManager, scanner);
        server.start();
    }
}