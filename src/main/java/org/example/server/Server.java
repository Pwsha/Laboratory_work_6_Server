package org.example.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server {
    private final int port;
    private final CommandExecutor commandExecutor;
    private volatile boolean running = true;
    private Selector selector;
    private ServerSocketChannel serverChannel;

    public Server(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    public void start() {
        try {
            selector = Selector.open();

            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Сервер запущен на порту " + port + " (неблокирующий режим)");

            while (running) {
                selector.select();

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        acceptConnection();
                    } else if (key.isReadable()) {
                        ClientHandler handler = (ClientHandler) key.attachment();
                        if (handler != null) {
                            handler.handleRead(key);
                        }
                    }
                }
            }

            selector.close();

        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    private void acceptConnection() throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);

        ClientHandler handler = new ClientHandler(clientChannel, commandExecutor);

        clientChannel.register(selector, SelectionKey.OP_READ, handler);

        System.out.println("Клиент подключился: " + clientChannel.getRemoteAddress());
    }

    public void stop() {
        running = false;
        if (selector != null) {
            selector.wakeup();
        }
    }
}