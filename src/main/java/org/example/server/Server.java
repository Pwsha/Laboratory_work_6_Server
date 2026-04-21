package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final int port;
    private final CommandExecutor commandExecutor;
    private volatile boolean running = true;

    public Server(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключился: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, commandExecutor);
                handler.handle();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
    }
}