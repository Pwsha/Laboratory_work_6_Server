package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.server.StudyGroupReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ExecuteScriptCommand {
    private final Client client;
    private final Scanner scanner;
    private final LinkedList<String> commandHistory;
    private final Set<String> executingScripts = new HashSet<>();

    public ExecuteScriptCommand(Client client, Scanner scanner, LinkedList<String> commandHistory) {
        this.client = client;
        this.scanner = scanner;
        this.commandHistory = commandHistory;
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

                // Рекурсивный вызов execute_script
                if (cmdName.equals("execute_script")) {
                    execute(cmdArgs);
                    continue;
                }

                // exit в скрипте
                if (cmdName.equals("exit")) {
                    System.out.println("  Завершение работы клиента");
                    client.disconnect();
                    return;
                }

                // history (локально)
                if (cmdName.equals("history")) {
                    printHistory();
                    continue;
                }

                // Добавляем в историю
                commandHistory.add(cmdName);
                if (commandHistory.size() > 5) commandHistory.removeFirst();

                // Используем StudyGroupReader для установки режима
                if (cmdArgs.startsWith("{") && cmdArgs.endsWith("}")) {
                    StudyGroupReader.setScriptMode(cmdArgs);
                } else {
                    StudyGroupReader.setConsoleMode();
                }

                CommandRequest request = buildRequest(cmdName, cmdArgs);
                if (request == null) {
                    StudyGroupReader.reset();
                    System.out.println("  Ошибка: неверный формат команды");
                    continue;
                }

                if (!client.isConnected() && !client.connect()) {
                    StudyGroupReader.reset();
                    System.out.println("  Ошибка: нет подключения к серверу");
                    continue;
                }

                CommandResponse response = client.sendRequest(request);
                printResponse(response);
                StudyGroupReader.reset();
            }

            System.out.println("Скрипт выполнен");

        } catch (FileNotFoundException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        } finally {
            executingScripts.remove(absolutePath);
            StudyGroupReader.reset();
        }
    }

    private CommandRequest buildRequest(String cmdName, String cmdArgs) {
        CommandType type = CommandType.fromString(cmdName);
        if (type == null) {
            return null;
        }

        CommandRequest.Builder builder = new CommandRequest.Builder().type(type);

        try {
            switch (type) {
                case HELP: case INFO: case SHOW: case CLEAR:
                    break;

                case ADD: case ADD_IF_MAX: case REMOVE_GREATER:
                    // StudyGroupReader сам определит, откуда читать (по установленному режиму)
                    builder.studyGroup(StudyGroupReader.read(null, scanner));
                    break;

                case UPDATE:
                    String[] updateArgs = cmdArgs.split("\\s+", 2);
                    if (updateArgs.length < 2) return null;
                    builder.id(Long.parseLong(updateArgs[0]));
                    builder.studyGroup(StudyGroupReader.read(null, scanner));
                    break;

                case REMOVE_BY_ID:
                    if (cmdArgs.isEmpty()) return null;
                    builder.id(Long.parseLong(cmdArgs));
                    break;

                case REMOVE_ANY_BY_STUDENTS_COUNT:
                    if (cmdArgs.isEmpty()) return null;
                    builder.studentsCount(Long.parseLong(cmdArgs));
                    break;

                case MIN_BY_SEMESTER_ENUM:
                    break;

                case COUNT_GREATER_THAN_EXPELLED_STUDENTS:
                    if (cmdArgs.isEmpty()) return null;
                    builder.expelledStudents(Integer.parseInt(cmdArgs));
                    break;

                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return builder.build();
    }

    private void printHistory() {
        if (commandHistory.isEmpty()) {
            System.out.println("  История команд пуста");
        } else {
            System.out.println("  Последние команды:");
            commandHistory.forEach(cmd -> System.out.println("    " + cmd));
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