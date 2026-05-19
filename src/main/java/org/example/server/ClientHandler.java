package org.example.server;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final CommandExecutor commandExecutor;

    public ClientHandler(Socket socket, CommandExecutor commandExecutor) {
        this.socket = socket;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void run() {
        System.out.println("Клиент подключился: " + socket.getInetAddress());

        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

            while (true) {
                try {
                    CommandRequest request = (CommandRequest) ois.readObject();

                    if (request == null) {
                        break;
                    }

                    System.out.println("Получена команда: " + request.getType());

                    CommandResponse response = commandExecutor.execute(request);
                    oos.writeObject(response);
                    oos.flush();

                    if (request.getType() == CommandType.EXIT) {
                        System.out.println("Клиент завершил работу");
                        break;
                    }

                } catch (EOFException e) {
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