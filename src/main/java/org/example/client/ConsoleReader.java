package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import static org.example.common.command.CommandType.*;
import org.example.common.init.StudyGroup;

import java.util.*;

public class ConsoleReader {
    private final Client client;
    private final Scanner scanner;
    private final LinkedList<String> commandHistory;
    private final ExecuteScript executeScriptCommand;
    private final HashSet<StudyGroup> emptyCollection = new HashSet<>();
    private final Map<CommandType, CommandHandler> handlers;

    @FunctionalInterface
    private interface CommandHandler {
        void handle(CommandRequest.Builder builder, String cmdArgs) throws Exception;
    }

    public ConsoleReader(Client client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.commandHistory = new LinkedList<>();
        this.executeScriptCommand = new ExecuteScript(client, scanner, this);
        this.handlers = new HashMap<>();
        initHandlers();
    }

    private void initHandlers() {
        CommandType[] noArgs = {HELP, INFO, SHOW, CLEAR, MIN_BY_SEMESTER_ENUM};
        for (CommandType type : noArgs) {
            handlers.put(type, (builder, args) -> {});
        }

        CommandHandler studyGroupHandler = (builder, args) ->
                builder.studyGroup(StudyGroupReader.read(emptyCollection, scanner));

        handlers.put(ADD, studyGroupHandler);
        handlers.put(ADD_IF_MAX, studyGroupHandler);
        handlers.put(REMOVE_GREATER, studyGroupHandler);

        handlers.put(UPDATE, (builder, args) -> {
            if (args.contains("{") && args.contains("}")) {
                String[] updateArgs = args.split("\\s+", 2);
                if (updateArgs.length < 2) {
                    throw new IllegalArgumentException("Формат: update id {element}");
                }
                builder.id(Long.parseLong(updateArgs[0]));
                StudyGroupReader.setScriptMode(updateArgs[1]);
                builder.studyGroup(StudyGroupReader.read(emptyCollection, scanner));
            } else {
                if (args.isEmpty()) {
                    throw new IllegalArgumentException("Формат: update id");
                }
                builder.id(Long.parseLong(args));
                StudyGroupReader.setConsoleMode();
                System.out.println("Введите новые данные для элемента с id " + Long.parseLong(args) + ":");
                builder.studyGroup(StudyGroupReader.read(emptyCollection, scanner));
            }
        });

        handlers.put(REMOVE_BY_ID, (builder, args) -> {
            if (args.isEmpty()) {
                throw new IllegalArgumentException("Формат: remove_by_id id");
            }
            builder.id(Long.parseLong(args));
        });

        handlers.put(REMOVE_ANY_BY_STUDENTS_COUNT, (builder, args) -> {
            if (args.isEmpty()) {
                throw new IllegalArgumentException("Формат: remove_any_by_students_count studentsCount");
            }
            builder.studentsCount(Long.parseLong(args));
        });

        handlers.put(COUNT_GREATER_THAN_EXPELLED_STUDENTS, (builder, args) -> {
            if (args.isEmpty()) {
                throw new IllegalArgumentException("Формат: count_greater_than_expelled_students expelledStudents");
            }
            builder.expelledStudents(Integer.parseInt(args));
        });
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
                printHistory();
                continue;
            }

            addToHistory(cmdName);

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

        CommandHandler handler = handlers.get(type);
        if (handler == null) {
            return null;
        }

        CommandRequest.Builder builder = new CommandRequest.Builder().type(type);

        try {
            handler.handle(builder, cmdArgs);
            return builder.build();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Ошибка: число введено неверно");
            return null;
        }
    }

    private void addToHistory(String command) {
        String[] parts = command.split("\\s+");
        String commandName = parts[0];

        commandHistory.add(commandName);
        if (commandHistory.size() > 5) {
            commandHistory.removeFirst();
        }
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