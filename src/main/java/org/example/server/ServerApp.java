package org.example.server;

import org.example.server.data.DataManager;

import java.util.Scanner;

public class ServerApp {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        System.out.println("=== Запуск сервера ===");
        System.out.println("Порт: " + port);

        DataManager dbManager = new DataManager();

        SessionManager sessionManager = new SessionManager();
        AuthManager authManager = new AuthManager(dbManager, sessionManager);

        CollectionManager collectionManager = new CollectionManager(dbManager);

        Scanner scanner = new Scanner(System.in);
        CommandExecutor commandExecutor = new CommandExecutor(collectionManager, authManager, scanner);

        Server server = new Server(port, commandExecutor);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nЗавершение работы сервера...");
            dbManager.close();
            commandExecutor.shutdown();
            scanner.close();
        }));

        server.start();
    }
}