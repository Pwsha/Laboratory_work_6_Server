package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.common.init.*;
import org.example.common.init.StudyGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.example.common.command.CommandType.*;

public class ExecuteScript {
    private final Client client;
    private final Scanner scanner;
    private final ConsoleReader consoleReader;
    private final HashSet<StudyGroup> emptyCollection = new HashSet<>();
    private final Set<String> executingScripts = new HashSet<>();
    private final Map<CommandType, ScriptCommandHandler> handlers;

    @FunctionalInterface
    private interface ScriptCommandHandler {
        void handle(CommandRequest.Builder builder, String cmdArgs) throws Exception;
    }

    public ExecuteScript(Client client, Scanner scanner, ConsoleReader consoleReader) {
        this.client = client;
        this.scanner = scanner;
        this.consoleReader = consoleReader;
        this.handlers = new HashMap<>();
        initHandlers();
    }

    private void initHandlers() {
        CommandType[] noArgs = {HELP, INFO, SHOW, CLEAR, HISTORY, MIN_BY_SEMESTER_ENUM};
        for (CommandType type : noArgs) {
            handlers.put(type, (builder, args) -> {});
        }

        ScriptCommandHandler groupHandler = (builder, args) -> {
            if (args.isEmpty() || !args.startsWith("{")) {
                throw new IllegalArgumentException("Ошибка: данные должны быть в фигурных скобках");
            }
            String parseInput = args.substring(1, args.length() - 1);
            builder.studyGroup(parseGroupFromString(parseInput));
        };

        handlers.put(ADD, groupHandler);
        handlers.put(ADD_IF_MAX, groupHandler);
        handlers.put(REMOVE_GREATER, groupHandler);

        handlers.put(UPDATE, (builder, args) -> {
            String[] updateArgs = args.split("\\s+", 2);
            if (updateArgs.length < 2) {
                throw new IllegalArgumentException("Формат: update id {element}");
            }
            if (!updateArgs[1].startsWith("{") || !updateArgs[1].endsWith("}")) {
                throw new IllegalArgumentException("Ошибка: данные должны быть в фигурных скобках");
            }
            builder.id(Long.parseLong(updateArgs[0]));
            String updateInput = updateArgs[1].substring(1, updateArgs[1].length() - 1);
            builder.studyGroup(parseGroupFromString(updateInput));
        });

        handlers.put(REMOVE_BY_ID, (builder, args) -> {
            if (args.isEmpty()) throw new IllegalArgumentException("Формат: remove_by_id id");
            builder.id(Long.parseLong(args));
        });

        handlers.put(REMOVE_ANY_BY_STUDENTS_COUNT, (builder, args) -> {
            if (args.isEmpty()) throw new IllegalArgumentException("Формат: remove_any_by_students_count studentsCount");
            builder.studentsCount(Long.parseLong(args));
        });

        handlers.put(COUNT_GREATER_THAN_EXPELLED_STUDENTS, (builder, args) -> {
            if (args.isEmpty()) throw new IllegalArgumentException("Формат: count_greater_than_expelled_students expelledStudents");
            builder.expelledStudents(Integer.parseInt(args));
        });
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

                if (cmdName.equals("execute_script")) {
                    execute(cmdArgs);
                    continue;
                }

                if (cmdName.equals("exit")) {
                    System.out.println("  Завершение работы клиента");
                    client.disconnect();
                    return;
                }

                CommandRequest request = buildRequest(cmdName, cmdArgs);
                if (request == null) {
                    System.out.println("  Ошибка: неверный формат команды");
                    continue;
                }

                if (!client.isConnected() && !client.connect()) {
                    System.out.println("  Ошибка: нет подключения к серверу");
                    continue;
                }

                CommandResponse response = client.sendRequest(request);

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

        ScriptCommandHandler handler = handlers.get(type);
        if (handler == null) {
            return null;
        }

        CommandRequest.Builder builder = new CommandRequest.Builder().type(type);

        try {
            handler.handle(builder, cmdArgs);
            return builder.build();
        } catch (IllegalArgumentException e) {
            System.out.println("  " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("  Ошибка: число введено неверно");
            return null;
        }
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