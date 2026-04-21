package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.client.StudyGroupReader;
import org.example.common.init.*;
import org.example.common.init.StudyGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ExecuteScript {
    private final Client client;
    private final Scanner scanner;
    private final ConsoleReader consoleReader;
    private final HashSet<StudyGroup> emptyCollection = new HashSet<>();
    private final Set<String> executingScripts = new HashSet<>();

    public ExecuteScript(Client client, Scanner scanner, ConsoleReader consoleReader) {
        this.client = client;
        this.scanner = scanner;
        this.consoleReader = consoleReader;
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

                // Разбираем строку на команду и аргументы
                String[] parts = line.split("\\s+", 2);
                String cmdName = parts[0].toLowerCase();
                String cmdArgs = parts.length > 1 ? parts[1] : "";

                // execute_script внутри скрипта - рекурсия
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

                // Создаём запрос для сервера
                CommandRequest request = buildRequest(cmdName, cmdArgs);
                if (request == null) {
                    System.out.println("  Ошибка: неверный формат команды");
                    continue;
                }

                // Отправляем на сервер
                if (!client.isConnected() && !client.connect()) {
                    System.out.println("  Ошибка: нет подключения к серверу");
                    continue;
                }

                CommandResponse response = client.sendRequest(request);

                // Выводим результат
                if (!response.isSuccess()) {
                    System.out.println("  Ошибка: " + response.getMessage());
                } else {
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

            System.out.println("Скрипт выполнен");

        } catch (FileNotFoundException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        } finally {
            executingScripts.remove(absolutePath);
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
                case HELP:
                case INFO:
                case SHOW:
                case CLEAR:
                case HISTORY:
                    // Команды без аргументов
                    break;

                case ADD:
                case ADD_IF_MAX:
                case REMOVE_GREATER:
                    // Парсим строку с данными
                    if (cmdArgs.isEmpty() || !cmdArgs.startsWith("{")) {
                        System.out.println("  Ошибка: данные должны быть в фигурных скобках");
                        return null;
                    }
                    String parseInput = cmdArgs.substring(1, cmdArgs.length() - 1);
                    builder.studyGroup(parseGroupFromString(parseInput));
                    break;

                case UPDATE:
                    // Формат: update id {element}
                    String[] updateArgs = cmdArgs.split("\\s+", 2);
                    if (updateArgs.length < 2) {
                        System.out.println("  Формат: update id {element}");
                        return null;
                    }
                    if (!updateArgs[1].startsWith("{") || !updateArgs[1].endsWith("}")) {
                        System.out.println("  Ошибка: данные должны быть в фигурных скобках");
                        return null;
                    }
                    builder.id(Long.parseLong(updateArgs[0]));
                    String updateInput = updateArgs[1].substring(1, updateArgs[1].length() - 1);
                    builder.studyGroup(parseGroupFromString(updateInput));
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
            System.out.println("  Ошибка: число введено неверно");
            return null;
        }

        return builder.build();
    }

    private StudyGroup parseGroupFromString(String input) {
        String[] parts = input.split(",");

        if (parts.length < 7) {
            throw new IllegalArgumentException("Недостаточно данных");
        }

        try {
            int idx = 0;
            String name = parts[idx++].trim();
            Float x = Float.parseFloat(parts[idx++].trim());
            Long y = Long.parseLong(parts[idx++].trim());
            long studentsCount = Long.parseLong(parts[idx++].trim());
            int expelledStudents = Integer.parseInt(parts[idx++].trim());
            FormOfEducation form = FormOfEducation.valueOf(parts[idx++].trim().toUpperCase());
            Semester semester = Semester.valueOf(parts[idx++].trim().toUpperCase());

            Coordinates coordinates = new Coordinates.Builder().x(x).y(y).build();

            return new StudyGroup.Builder()
                    .name(name)
                    .coordinates(coordinates)
                    .studentsCount(studentsCount)
                    .expelledStudents(expelledStudents)
                    .formOfEducation(form)
                    .semesterEnum(semester)
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка парсинга: " + e.getMessage());
        }
    }
}