package org.example.server;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import java.io.*;
import java.net.Socket;

public class ClientHandler {
    private final Socket socket;
    private final CommandExecutor executor;

    public ClientHandler(Socket socket, CommandExecutor executor) {
        this.socket = socket;
        this.executor = executor;
    }

    public void handle() {
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

            CommandRequest request = (CommandRequest) ois.readObject();
            System.out.println("Получена команда: " + request.getType());

            CommandResponse response = executor.execute(request);
            oos.writeObject(response);
            oos.flush();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка обработки клиента: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}