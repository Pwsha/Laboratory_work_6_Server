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
                System.out.println("Завершение работы клиента");
                client.disconnect();
                break;
            }

            if (cmdName.equals("execute_script")) {
                executeScriptCommand.execute(cmdArgs);
                continue;
            }

            if (cmdName.equals("history")) {
                history.print_history();
                continue;
            }

            history.add(cmdName);

            CommandRequest request = commandBuilder.build(cmdName, cmdArgs);
            if (request == null) {
                continue;
            }

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