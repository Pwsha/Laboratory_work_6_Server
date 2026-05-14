package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ExecuteScript {
    private final Client client;
    private final CommandBuilder commandBuilder;
    private String authToken = null;
    private final Set<String> executingScripts = new HashSet<>();

    public ExecuteScript(Client client, CommandBuilder commandBuilder) {
        this.client = client;
        this.commandBuilder = commandBuilder;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void execute(String filename) {
        if (filename == null || filename.isEmpty()) {
            System.out.println("Ошибка: укажите имя файла");
            return;
        }

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Файл не найден: " + filename);
            return;
        }

        String absolutePath = file.getAbsolutePath();
        if (executingScripts.contains(absolutePath)) {
            System.out.println("Ошибка: обнаружен рекурсивный вызов скрипта " + filename);
            return;
        }

        executingScripts.add(absolutePath);
        System.out.println("Выполнение скрипта: " + filename);

        commandBuilder.setScriptMode(true);

        try (Scanner fileScanner = new Scanner(file)) {
            int lineNumber = 0;

            while (fileScanner.hasNextLine()) {
                lineNumber++;
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                System.out.println("[" + lineNumber + "] " + line);

                String[] parts = line.split("\\s+", 2);
                String cmdName = parts[0].toLowerCase();
                String cmdArgs = parts.length > 1 ? parts[1] : "";

                if (cmdName.equals("execute_script")) {
                    execute(cmdArgs);
                    continue;
                }

                if (cmdName.equals("exit")) {
                    System.out.println("  Завершение работы клиента");
                    client.disconnect();
                    return;
                }

                CommandRequest request = commandBuilder.build(cmdName, cmdArgs, authToken);
                if (request == null) {
                    System.out.println("  Ошибка: неверный формат команды");
                    continue;
                }

                if (!client.isConnected() && !client.connect()) {
                    System.out.println("  Ошибка: нет подключения к серверу");
                    continue;
                }

                CommandResponse response = client.sendRequest(request);
                printResponse(response);
            }

            System.out.println("Скрипт выполнен");

        } catch (FileNotFoundException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        } finally {
            commandBuilder.setScriptMode(false);
            executingScripts.remove(absolutePath);
        }
    }

    private void printResponse(CommandResponse response) {
        if (!response.isSuccess()) {
            System.out.println("  Ошибка: " + response.getMessage());
            return;
        }

        System.out.println("  " + response.getMessage());

        if (response.getCollection() != null && !response.getCollection().isEmpty()) {
            response.getCollection().forEach(g -> System.out.println("    " + g));
        }
        if (response.getGroup() != null) {
            System.out.println("    " + response.getGroup());
        }
        if (response.getCount() != null) {
            System.out.println("    " + response.getCount());
        }
    }
}