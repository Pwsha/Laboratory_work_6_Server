package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import java.io.*;
import java.net.Socket;

public class Client {
    private final String host;
    private final int port;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean connected = false;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            connected = true;
            System.out.println("Подключено к серверу " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("Не удалось подключиться: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    public CommandResponse sendRequest(CommandRequest request) {
        if (!connected) {
            return CommandResponse.error("Нет подключения к серверу");
        }

        try {
            oos.writeObject(request);
            oos.flush();
            return (CommandResponse) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            connected = false;  // ← сбрасываем флаг при ошибке
            return CommandResponse.error("Ошибка связи: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public void disconnect() {
        connected = false;
        try { if (oos != null) oos.close(); } catch (IOException ignored) {}
        try { if (ois != null) ois.close(); } catch (IOException ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }
}