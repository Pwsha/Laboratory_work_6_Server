//package org.example.client;
//
//import org.example.common.command.CommandRequest;
//import org.example.common.command.CommandResponse;
//import org.example.common.command.CommandType;
//import org.example.server.Command;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Scanner;
//import java.util.Set;
//
///**
// * Команда для выполнения скрипта из файла.
// * @author Pwsha
// * @version v2.0
// */
//public class ExecuteScriptCommand implements Command {
//
//    private final Map<CommandType, Command> commands;
//    private static Set<String> executingScripts = new HashSet<>();
//
//    public ExecuteScriptCommand(Map<CommandType, Command> commands) {
//        this.commands = commands;
//    }
//
//    @Override
//    public CommandResponse execute(CommandRequest request) {
//        String filename = request.getStringArg();
//        if (filename == null || filename.isEmpty()) {
//            return CommandResponse.error("Ошибка: укажите имя файла");
//        }
//
//        File scriptFile = new File(filename);
//
//        if (!scriptFile.exists()) {
//            return CommandResponse.error("Ошибка: файл не найден: " + filename);
//        }
//
//        String absolutePath = scriptFile.getAbsolutePath();
//        if (executingScripts.contains(absolutePath)) {
//            return CommandResponse.error("Ошибка: обнаружен рекурсивный вызов скрипта " + filename);
//        }
//
//        executingScripts.add(absolutePath);
//        StringBuilder result = new StringBuilder();
//        result.append("Выполнение скрипта: ").append(filename).append("\n");
//
//        try (Scanner scriptScanner = new Scanner(scriptFile)) {
//            int lineNumber = 0;
//            int executedCommands = 0;
//
//            while (scriptScanner.hasNextLine()) {
//                lineNumber++;
//                String line = scriptScanner.nextLine().trim();
//
//                if (line.isEmpty() || line.startsWith("#")) continue;
//
//                result.append("[").append(lineNumber).append("] ").append(line).append("\n");
//
//                String[] parts = line.split("\\s+", 2);
//                String cmdName = parts[0].toLowerCase();
//                String cmdArgs = parts.length > 1 ? parts[1] : "";
//
//                Command command = commands.get(CommandType.fromString(cmdName));
//                if (command == null) {
//                    result.append("  Ошибка: неизвестная команда '").append(cmdName).append("'\n");
//                    continue;
//                }
//
//                try {
//                    CommandRequest nestedRequest = buildNestedRequest(cmdName, cmdArgs);
//                    if (nestedRequest == null) {
//                        result.append("  Ошибка: неверный формат команды\n");
//                        continue;
//                    }
//
//                    CommandResponse response = command.execute(nestedRequest);
//                    result.append("  ").append(response.getMessage()).append("\n");
//                    executedCommands++;
//
//                } catch (Exception e) {
//                    result.append("  Ошибка: ").append(e.getMessage()).append("\n");
//                }
//            }
//
//            result.append("Скрипт выполнен. Выполнено команд: ").append(executedCommands);
//
//        } catch (FileNotFoundException e) {
//            result.append("Ошибка при чтении файла: ").append(e.getMessage());
//        } finally {
//            executingScripts.remove(absolutePath);
//        }
//
//        return CommandResponse.success(result.toString());
//    }
//
//    private CommandRequest buildNestedRequest(String cmdName, String cmdArgs) {
//        CommandType type = CommandType.fromString(cmdName);
//        if (type == null) return null;
//
//        CommandRequest.Builder builder = new CommandRequest.Builder().type(type);
//
//        try {
//            switch (type) {
//                case HELP:
//                case INFO:
//                case SHOW:
//                case CLEAR:
//                case HISTORY:
//                    break;
//
//                case ADD:
//                case ADD_IF_MAX:
//                case REMOVE_GREATER:
//                    return null;
//
//                case UPDATE:
//                    String[] updateArgs = cmdArgs.split("\\s+", 2);
//                    if (updateArgs.length < 2) return null;
//                    builder.id(Long.parseLong(updateArgs[0]));
//                    // Для обновления тоже нужен StudyGroup
//                    return null;
//
//                case REMOVE_BY_ID:
//                    if (cmdArgs.isEmpty()) return null;
//                    builder.id(Long.parseLong(cmdArgs));
//                    break;
//
//                case REMOVE_ANY_BY_STUDENTS_COUNT:
//                    if (cmdArgs.isEmpty()) return null;
//                    builder.studentsCount(Long.parseLong(cmdArgs));
//                    break;
//
//                case MIN_BY_SEMESTER_ENUM:
//                    break;
//
//                case COUNT_GREATER_THAN_EXPELLED_STUDENTS:
//                    if (cmdArgs.isEmpty()) return null;
//                    builder.expelledStudents(Integer.parseInt(cmdArgs));
//                    break;
//
//                case EXECUTE_SCRIPT:
//                    builder.stringArg(cmdArgs);
//                    break;
//
//                default:
//                    return null;
//            }
//        } catch (NumberFormatException e) {
//            return null;
//        }
//
//        return builder.build();
//    }
//
//    @Override
//    public String getName() {
//        return "execute_script";
//    }
//
//    @Override
//    public String getDescription() {
//        return "считать и исполнить скрипт из указанного файла";
//    }
//
//    @Override
//    public String getSyntax() {
//        return "execute_script file_name";
//    }
//}