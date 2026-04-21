package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.server.StudyGroupReader;

import java.util.*;

public class ConsoleReader {
    private final Client client;
    private final Scanner scanner;
    private final LinkedList<String> commandHistory;
    private final ExecuteScriptCommand executeScriptCommand;

    public ConsoleReader(Client client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.commandHistory = new LinkedList<>();
        this.executeScriptCommand = new ExecuteScriptCommand(client, scanner, commandHistory);
    }

    public void start() {
        System.out.println("Клиент запущен. Введите 'help' для справки.");

        while (true) {
            System.out.print("> ");
            String input;

            try {
                input = scanner.nextLine().trim();
            } catch (NoSuchElementException e) {
                break;
            }

            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+", 2);
            String cmdName = parts[0].toLowerCase();
            String cmdArgs = parts.length > 1 ? parts[1] : "";

            if (cmdName.equals("execute_script")) {
                executeScriptCommand.execute(cmdArgs);
                continue;
            }

            if (cmdName.equals("history")) {
                printHistory();
                continue;
            }

            commandHistory.add(cmdName);
            if (commandHistory.size() > 5) commandHistory.removeFirst();

            if (!cmdArgs.isEmpty() && cmdArgs.startsWith("{") && cmdArgs.endsWith("}")) {
                StudyGroupReader.setScriptMode(cmdArgs);
            } else {
                StudyGroupReader.setConsoleMode();
            }

            CommandRequest request = buildRequest(cmdName, cmdArgs);
            if (request == null) {
                StudyGroupReader.reset();
                continue;
            }

            if (!client.isConnected() && !client.connect()) {
                System.out.println("Нет подключения к серверу");
                StudyGroupReader.reset();
                continue;
            }

            CommandResponse response = client.sendRequest(request);
            printResponse(response);
            StudyGroupReader.reset();
        }

        scanner.close();
    }

    public CommandRequest buildRequest(String cmdName, String cmdArgs) {
        CommandType type = CommandType.fromString(cmdName);
        if (type == null) {
            System.out.println("Неизвестная команда");
            return null;
        }

        CommandRequest.Builder builder = new CommandRequest.Builder().type(type);

        try {
            switch (type) {
                case HELP: case INFO: case SHOW: case CLEAR:
                    break;

                case ADD: case ADD_IF_MAX: case REMOVE_GREATER:
                    builder.studyGroup(StudyGroupReader.read(null, scanner));
                    break;

                case UPDATE:
                    String[] updateArgs = cmdArgs.split("\\s+", 2);
                    if (updateArgs.length < 2) {
                        System.out.println("Формат: update id {element}");
                        return null;
                    }
                    builder.id(Long.parseLong(updateArgs[0]));
                    builder.studyGroup(StudyGroupReader.read(null, scanner));
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
                    return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: число введено неверно");
            return null;
        }

        return builder.build();
    }

    public void printHistory() {
        if (commandHistory.isEmpty()) {
            System.out.println("История команд пуста");
        } else {
            System.out.println("Последние команды:");
            commandHistory.forEach(cmd -> System.out.println("  " + cmd));
        }
    }

    public void printResponse(CommandResponse response) {
        if (!response.isSuccess()) {
            System.out.println("Ошибка: " + response.getMessage());
            return;
        }

        System.out.println(response.getMessage());

        if (response.getCollection() != null && !response.getCollection().isEmpty()) {
            response.getCollection().forEach(g -> System.out.println("  " + g));
        }
        if (response.getGroup() != null) {
            System.out.println("  " + response.getGroup());
        }
        if (response.getCount() != null) {
            System.out.println("  " + response.getCount());
        }
    }
}