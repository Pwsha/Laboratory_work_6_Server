package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;

import java.util.*;

public class ConsoleReader {
    private final Client client;
    private final Scanner scanner;
    private final History history;
    private final CommandBuilder commandBuilder;
    private final OutputRequest outputRequest;
    private final ExecuteScript executeScriptCommand;
    private String authToken = null;
    private String currentLogin = null;

    public ConsoleReader(Client client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.history = new History();
        this.commandBuilder = new CommandBuilder(scanner);
        this.outputRequest = new OutputRequest();
        this.executeScriptCommand = new ExecuteScript(client, commandBuilder);
    }

    public void start() {
        System.out.println("Клиент запущен. Введите 'help' для справки.");
        System.out.println("Сначала зарегистрируйтесь (register) или войдите (login)");

        while (true) {
            System.out.print("> ");
            String input;

            try {
                input = scanner.nextLine().trim();
            } catch (NoSuchElementException e) {
                System.out.println("\nЗавершение программы...");
                break;
            }

            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+", 2);
            String cmdName = parts[0].toLowerCase();
            String cmdArgs = parts.length > 1 ? parts[1] : "";

            // Выход
            if (cmdName.equals("exit")) {
                if (authToken != null) {
                    sendLogout();
                }
                System.out.println("Завершение работы клиента");
                client.disconnect();
                break;
            }

            // LOGIN
            if (cmdName.equals("login")) {
                handleLogin(cmdArgs);
                continue;
            }

            // REGISTER
            if (cmdName.equals("register")) {
                handleRegister(cmdArgs);
                continue;
            }

            // Проверка авторизации
            if (authToken == null) {
                System.out.println("Ошибка: необходимо войти (login) или зарегистрироваться (register)");
                continue;
            }

            executeScriptCommand.setAuthToken(authToken);

            if (cmdName.equals("execute_script")) {
                executeScriptCommand.execute(cmdArgs);
                continue;
            }

            if (cmdName.equals("history")) {
                history.print_history();
                continue;
            }

            if (cmdName.equals("logout")) {
                handleLogout();
                continue;
            }

            history.add(cmdName);

            CommandRequest request = buildRequestWithToken(cmdName, cmdArgs);
            if (request == null) continue;

            if (!client.isConnected() && !client.connect()) {
                System.out.println("Нет подключения к серверу");
                continue;
            }

            CommandResponse response = client.sendRequest(request);
            outputRequest.printResponse(response);
        }

        scanner.close();
    }

    private CommandRequest buildRequestWithToken(String cmdName, String cmdArgs) {
        CommandType type = CommandType.fromString(cmdName);
        if (type == null) {
            System.out.println("Неизвестная команда");
            return null;
        }

        CommandRequest.Builder builder = new CommandRequest.Builder()
                .type(type)
                .stringArg(authToken);  // добавляем токен

        var handler = commandBuilder.getHandler(type);
        if (handler == null) {
            return null;
        }

        try {
            handler.accept(builder, cmdArgs);
            return builder.build();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Ошибка: число введено неверно");
            return null;
        }
    }

    private void handleLogin(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length < 2) {
            System.out.println("Формат: login логин пароль");
            return;
        }

        String login = parts[0];
        String password = parts[1];

        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.LOGIN)
                .login(login)
                .password(password)
                .build();

        if (!client.isConnected() && !client.connect()) {
            System.out.println("Нет подключения к серверу");
            return;
        }

        CommandResponse response = client.sendRequest(request);
        outputRequest.printResponse(response);

        if (response.isSuccess() && response.getMessage().contains("Ваш токен:")) {
            String msg = response.getMessage();
            authToken = msg.substring(msg.indexOf("Ваш токен:") + 11).trim();
            currentLogin = login;
            System.out.println("Авторизован как: " + currentLogin);
            executeScriptCommand.setAuthToken(authToken);
        }
    }

    private void handleRegister(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length < 2) {
            System.out.println("Формат: register логин пароль");
            return;
        }

        String login = parts[0];
        String password = parts[1];

        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.REGISTER)
                .login(login)
                .password(password)
                .build();

        if (!client.isConnected() && !client.connect()) {
            System.out.println("Нет подключения к серверу");
            return;
        }

        CommandResponse response = client.sendRequest(request);
        outputRequest.printResponse(response);
    }

    private void handleLogout() {
        if (authToken == null) {
            System.out.println("Вы не авторизованы");
            return;
        }
        sendLogout();
        authToken = null;
        currentLogin = null;
        executeScriptCommand.setAuthToken(null);
        System.out.println("Вы вышли из системы");
    }

    private void sendLogout() {
        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.LOGOUT)
                .stringArg(authToken)
                .build();

        if (client.isConnected()) {
            CommandResponse response = client.sendRequest(request);
            outputRequest.printResponse(response);
        }
    }
}