package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;

import java.util.*;

public class ConsoleReader {
    private final Client client;
    private final Scanner scanner;
    private final History history;
    private final CommandBuilder commandBuilder;
    private final OutputRequest outputRequest;
    private final ExecuteScript executeScriptCommand;
    private final AuthHandler authHandler;

    public ConsoleReader(Client client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.history = new History();
        this.commandBuilder = new CommandBuilder(scanner);
        this.outputRequest = new OutputRequest();
        this.authHandler = new AuthHandler(client, outputRequest);
        this.executeScriptCommand = new ExecuteScript(client, commandBuilder);
    }

    public void start() {
        System.out.println("Клиент запущен. Введите 'help' для справки.");
        System.out.println("Сначала зарегистрируйтесь (register) или войдите (login)");

        commandBuilder.setScriptMode(false);

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

            if (cmdName.equals("exit")) {
                if (authHandler.isAuthenticated()) {
                    authHandler.handleLogout();
                }
                System.out.println("Завершение работы клиента");
                client.disconnect();
                break;
            }

            if (cmdName.equals("login")) {
                authHandler.handleLogin(cmdArgs);
                executeScriptCommand.setAuthToken(authHandler.getAuthToken());
                continue;
            }

            if (cmdName.equals("register")) {
                authHandler.handleRegister(cmdArgs);
                continue;
            }

            if (!authHandler.isAuthenticated()) {
                System.out.println("Ошибка: необходимо войти (login) или зарегистрироваться (register)");
                continue;
            }

            executeScriptCommand.setAuthToken(authHandler.getAuthToken());

            if (cmdName.equals("execute_script")) {
                executeScriptCommand.execute(cmdArgs);
                continue;
            }

            if (cmdName.equals("history")) {
                history.print_history();
                continue;
            }

            if (cmdName.equals("logout")) {
                authHandler.handleLogout();
                executeScriptCommand.setAuthToken(null);
                continue;
            }

            history.add(cmdName);

            CommandRequest request = commandBuilder.build(cmdName, cmdArgs, authHandler.getAuthToken());
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
}