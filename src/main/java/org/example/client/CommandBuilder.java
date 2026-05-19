package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandType;
import org.example.common.init.StudyGroup;

import java.util.*;
import java.util.function.BiConsumer;

import static org.example.common.command.CommandType.*;

public class CommandBuilder {
    private final Scanner scanner;
    private final HashSet<StudyGroup> emptyCollection = new HashSet<>();
    private final Map<CommandType, BiConsumer<CommandRequest.Builder, String>> handlers = new HashMap<>();
    private boolean isScriptMode = false;  // флаг режима скрипта

    public CommandBuilder(Scanner scanner) {
        this.scanner = scanner;
        initHandlers();
    }

    public void setScriptMode(boolean isScriptMode) {
        this.isScriptMode = isScriptMode;
    }

    private void initHandlers() {

        CommandType[] noArgs = {HELP, INFO, SHOW, CLEAR, MIN_BY_SEMESTER_ENUM, HISTORY};
        for (CommandType type : noArgs) {
            handlers.put(type, (builder, args) -> {});
        }

        BiConsumer<CommandRequest.Builder, String> groupHandler = (builder, args) -> {
            if (isScriptMode || (args != null && args.startsWith("{") && args.endsWith("}"))) {
                String parseInput = args.substring(1, args.length() - 1);
                builder.studyGroup(GroupParser.parseFromString(parseInput, emptyCollection));
            }
            else {
                builder.studyGroup(StudyGroupReader.read(emptyCollection, scanner));
            }
        };
        handlers.put(ADD, groupHandler);
        handlers.put(ADD_IF_MAX, groupHandler);
        handlers.put(REMOVE_GREATER, groupHandler);

        handlers.put(UPDATE, (builder, args) -> {
            if (isScriptMode || (args != null && args.contains("{") && args.contains("}"))) {
                String[] updateArgs = args.split("\\s+", 2);
                if (updateArgs.length < 2) {
                    throw new IllegalArgumentException("Формат: update id {element}");
                }
                builder.id(Long.parseLong(updateArgs[0]));
                String updateInput = updateArgs[1].substring(1, updateArgs[1].length() - 1);
                builder.studyGroup(GroupParser.parseFromString(updateInput, emptyCollection));
            } else {
                if (args.isEmpty()) {
                    throw new IllegalArgumentException("Формат: update id");
                }
                builder.id(Long.parseLong(args));
                System.out.println("Введите новые данные для элемента с id " + Long.parseLong(args) + ":");
                builder.studyGroup(StudyGroupReader.read(emptyCollection, scanner));
            }
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

    public BiConsumer<CommandRequest.Builder, String> getHandler(CommandType type) {
        return handlers.get(type);
    }

    public CommandRequest build(String cmdName, String cmdArgs, String authToken) {
        CommandType type = CommandType.fromString(cmdName);
        if (type == null) {
            System.out.println("Неизвестная команда");
            return null;
        }

        BiConsumer<CommandRequest.Builder, String> handler = handlers.get(type);
        if (handler == null) {
            return null;
        }

        CommandRequest.Builder builder = new CommandRequest.Builder()
                .type(type)
                .stringArg(authToken);

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
}