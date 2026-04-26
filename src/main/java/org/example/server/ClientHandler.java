package org.example.server;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientHandler {
    private final SocketChannel channel;
    private final CommandExecutor commandExecutor;
    private int expectedLength = -1;
    private ByteBuffer dataBuffer = null;

    public ClientHandler(SocketChannel channel, CommandExecutor commandExecutor) {
        this.channel = channel;
        this.commandExecutor = commandExecutor;
    }

    public void handleRead(SelectionKey key) {
        try {
            if (expectedLength == -1) {
                ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                int read = channel.read(lengthBuffer);

                if (read == -1) {
                    closeConnection(key);
                    return;
                }

                if (lengthBuffer.position() == Integer.BYTES) {
                    lengthBuffer.flip();
                    expectedLength = lengthBuffer.getInt();
                    dataBuffer = ByteBuffer.allocate(expectedLength);
                } else {
                    return;
                }
            }

            int read = channel.read(dataBuffer);
            if (read == -1) {
                closeConnection(key);
                return;
            }

            if (dataBuffer.position() == expectedLength) {
                dataBuffer.flip();
                byte[] data = new byte[expectedLength];
                dataBuffer.get(data);

                // Десериализация и выполнение
                try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                     ObjectInputStream ois = new ObjectInputStream(bais)) {

                    CommandRequest request = (CommandRequest) ois.readObject();
                    System.out.println("Получена команда: " + request.getType());

                    CommandResponse response = commandExecutor.execute(request);
                    sendResponse(response);
                }

                expectedLength = -1;
                dataBuffer = null;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
            closeConnection(key);
        }
    }

    private void sendResponse(CommandResponse response) throws IOException {
        // Сериализация
        byte[] data;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(response);
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
    }

    private void closeConnection(SelectionKey key) {
        try {
            System.out.println("Клиент отключился");
            key.cancel();
            channel.close();
        } catch (IOException e) {
        }
    }
}