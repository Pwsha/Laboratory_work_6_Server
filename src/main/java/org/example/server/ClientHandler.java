package org.example.server;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;

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

            // Цикл обработки команд от одного клиента
            while (true) {
                try {
                    CommandRequest request = (CommandRequest) ois.readObject();

                    // Если клиент закрыл соединение
                    if (request == null) {
                        break;
                    }

                    System.out.println("Получена команда: " + request.getType());

                    CommandResponse response = executor.execute(request);
                    oos.writeObject(response);
                    oos.flush();

                    if (request.getType() == CommandType.EXIT) {
                        break;
                    }

                } catch (EOFException e) {
                    // Клиент закрыл соединение
                    System.out.println("Клиент отключился");
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("Ошибка десериализации: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка обработки клиента: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("Соединение с клиентом закрыто");
            } catch (IOException ignored) {}
        }
    }
}