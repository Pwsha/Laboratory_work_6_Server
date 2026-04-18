package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.common.init.StudyGroup;
import org.example.server.CommandHelper;

import java.io.File;
import java.util.*;

public class ConsoleReader {
    private final Client client;
    private final Scanner scanner;
    private final LinkedList<String> commandHistory;

    public ConsoleReader(Client client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.commandHistory = new LinkedList<>();
    }

    public void start() {
        System.out.println("Клиент запущен. Введите 'help' для справки.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
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
                executeScript(cmdArgs);
                continue;
            }

            addToHistory(cmdName);

            CommandRequest request = buildRequest(cmdName, cmdArgs);
            if (request == null) continue;

            if (!client.isConnected() && !client.connect()) {
                System.out.println("Нет подключения к серверу");
                continue;
            }

            CommandResponse response = client.sendRequest(request);
            printResponse(response);
        }

        scanner.close();
    }

    private CommandRequest buildRequest(String cmdName, String cmdArgs) {
        CommandType type = CommandType.fromString(cmdName);
        if (type == null) {
            System.out.println("Неизвестная команда");
            return null;
        }

        CommandRequest.Builder builder = new CommandRequest.Builder().type(type);

        try {
            switch (type) {
                case HELP: case INFO: case SHOW: case CLEAR: case HISTORY:
                    break;

                case ADD: case ADD_IF_MAX: case REMOVE_GREATER:
                    builder.studyGroup(CommandHelper.readStudyGroup(scanner, null));
                    break;

                case UPDATE:
                    String[] updateArgs = cmdArgs.split("\\s+", 2);
                    if (updateArgs.length < 2) {
                        System.out.println("Формат: update id {element}");
                        return null;
                    }
                    builder.id(Long.parseLong(updateArgs[0]));
                    builder.studyGroup(CommandHelper.readStudyGroup(scanner, null));
                    break;

                case REMOVE_BY_ID:
                    if (cmdArgs.isEmpty()) {
                        System.out.println("Формат: remove_by_id id");
                        return null;
                    }
                    builder.id(Long.parseLong(cmdArgs));
                    break;

                case REMOVE_ANY_BY_STUDENTS_COUNT:
                    if (cmdArgs.isEmpty()) {
                        System.out.println("Формат: remove_any_by_students_count studentsCount");
                        return null;
                    }
                    builder.studentsCount(Long.parseLong(cmdArgs));
                    break;

                case MIN_BY_SEMESTER_ENUM:
                    break;

                case COUNT_GREATER_THAN_EXPELLED_STUDENTS:
                    if (cmdArgs.isEmpty()) {
                        System.out.println("Формат: count_greater_than_expelled_students expelledStudents");
                        return null;
                    }
                    builder.expelledStudents(Integer.parseInt(cmdArgs));
                    break;

                default:
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: число введено неверно");
            return null;
        }

        return builder.build();
    }

    private void executeScript(String filename) {
        if (filename.isEmpty()) {
            System.out.println("Ошибка: укажите имя файла");
            return;
        }

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Файл не найден");
            return;
        }

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+", 2);
                String cmdName = parts[0].toLowerCase();
                String cmdArgs = parts.length > 1 ? parts[1] : "";

                if (cmdName.equals("execute_script")) {
                    executeScript(cmdArgs);
                    continue;
                }

                if (cmdName.equals("exit")) {
                    System.out.println("Завершение работы клиента");
                    client.disconnect();
                    return;
                }

                CommandRequest request = buildRequest(cmdName, cmdArgs);
                if (request == null) continue;

                if (!client.isConnected() && !client.connect()) {
                    System.out.println("Нет подключения к серверу");
                    continue;
                }

                CommandResponse response = client.sendRequest(request);
                printResponse(response);
            }
        } catch (Exception e) {
            System.out.println("Ошибка чтения скрипта: " + e.getMessage());
        }
    }

    private void printResponse(CommandResponse response) {
        if (!response.isSuccess()) {
            System.out.println("Ошибка: " + response.getMessage());
            return;
        }

        System.out.println(response.getMessage());

        if (response.getCollection() != null) {
            response.getCollection().forEach(g -> System.out.println("  " + g));
        }
        if (response.getGroup() != null) {
            System.out.println("  " + response.getGroup());
        }
        if (response.getCount() != null) {
            System.out.println("  " + response.getCount());
        }
    }

    private void addToHistory(String command) {
        commandHistory.add(command);
        if (commandHistory.size() > 5) commandHistory.removeFirst();
    }
}