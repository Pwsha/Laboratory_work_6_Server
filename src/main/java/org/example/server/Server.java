package org.example.server;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final CommandExecutor commandExecutor;
    private final ExecutorService acceptPool;
    private final ExecutorService readPool;
    private final ExecutorService sendPool;
    private volatile boolean running = true;

    public Server(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
        this.acceptPool = Executors.newCachedThreadPool();
        this.readPool = Executors.newCachedThreadPool();
        this.sendPool = Executors.newCachedThreadPool();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключился: " + clientSocket.getInetAddress());

                // Приём в отдельном потоке (Cached thread pool)
                acceptPool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {

            while (true) {
                CommandRequest request = readRequest(ois);
                if (request == null) break;

                CommandResponse response = commandExecutor.execute(request);

                sendResponse(oos, response);
            }

        } catch (IOException e) {
            System.err.println("Ошибка обработки клиента: " + e.getMessage());
        }
    }

    private CommandRequest readRequest(ObjectInputStream ois) {
        try {
            return (CommandRequest) ois.readObject();
        } catch (EOFException e) {
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
            return null;
        }
    }

    private void sendResponse(ObjectOutputStream oos, CommandResponse response) {
        try {
            oos.writeObject(response);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Ошибка отправки: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        acceptPool.shutdown();
        readPool.shutdown();
        sendPool.shutdown();
        commandExecutor.shutdown();
    }
}