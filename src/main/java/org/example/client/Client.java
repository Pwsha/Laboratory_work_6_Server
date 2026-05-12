package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    private final String host;
    private final int port;
    private SocketChannel channel;
    private boolean connected = false;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));

            while (!channel.finishConnect()) {
                Thread.sleep(10);
            }

            connected = true;
            System.out.println("Подключено к серверу " + host + ":" + port);
            return true;

        } catch (IOException | InterruptedException e) {
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
            // Сериализация
            byte[] data;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(request);
                oos.flush();
                data = baos.toByteArray();
            }

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
            buffer.putInt(data.length);
            buffer.put(data);
            buffer.flip();

            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
            int read = 0;
            while (read < Integer.BYTES) {
                int r = channel.read(lengthBuffer);
                if (r == -1) {
                    throw new IOException("Сервер закрыл соединение");
                }
                if (r == 0) {
                    Thread.sleep(10);
                    continue;
                }
                read += r;
            }

            lengthBuffer.flip();
            int responseLength = lengthBuffer.getInt();

            ByteBuffer responseBuffer = ByteBuffer.allocate(responseLength);
            read = 0;
            while (read < responseLength) {
                int r = channel.read(responseBuffer);
                if (r == -1) {
                    throw new IOException("Сервер закрыл соединение");
                }
                if (r == 0) {
                    Thread.sleep(10);
                    continue;
                }
                read += r;
            }

            responseBuffer.flip();
            byte[] responseData = new byte[responseLength];
            responseBuffer.get(responseData);

            // Десериализация
            try (ByteArrayInputStream bais = new ByteArrayInputStream(responseData);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (CommandResponse) ois.readObject();
            }

        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            connected = false;
            return CommandResponse.error("Ошибка связи: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && channel != null && channel.isConnected();
    }

    public void disconnect() {
        connected = false;
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (IOException ignored) {}
    }
}