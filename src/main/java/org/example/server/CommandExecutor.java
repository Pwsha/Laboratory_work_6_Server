package org.example.server;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.server.list.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandExecutor {
    private final Map<CommandType, Command> commands = new HashMap<>();
    private final CollectionManager manager;
    private final Scanner scanner;

    public CommandExecutor(CollectionManager manager, Scanner scanner) {
        this.manager = manager;
        this.scanner = scanner;
        initCommands();
    }

    private void initCommands() {
        commands.put(CommandType.INFO, new InfoCommand(manager));
        commands.put(CommandType.SHOW, new ShowCommand(manager));
        commands.put(CommandType.ADD, new AddCommand(manager));
        commands.put(CommandType.UPDATE, new UpdateCommand(manager));
        commands.put(CommandType.REMOVE_BY_ID, new RemoveByIdCommand(manager));
        commands.put(CommandType.EXIT, new ExitCommand(manager));
        commands.put(CommandType.CLEAR, new ClearCommand(manager));
        commands.put(CommandType.ADD_IF_MAX, new AddIfMaxCommand(manager));
        commands.put(CommandType.REMOVE_GREATER, new RemoveGreaterCommand(manager));
        commands.put(CommandType.REMOVE_ANY_BY_STUDENTS_COUNT, new RemoveAnyByStudentsCountCommand(manager));
        commands.put(CommandType.MIN_BY_SEMESTER_ENUM, new MinBySemesterEnumCommand(manager));
        commands.put(CommandType.COUNT_GREATER_THAN_EXPELLED_STUDENTS, new CountGreaterThanExpelledStudentsCommand(manager));
        commands.put(CommandType.HISTORY, new HistoryCommand());
        commands.put(CommandType.EXECUTE_SCRIPT, new ExecuteScriptCommand());
        commands.put(CommandType.HELP, new HelpCommand(commands));
    }

    public CommandResponse execute(CommandRequest request) {
        Command command = commands.get(request.getType());
        if (command == null) {
            return CommandResponse.error("Неизвестная команда");
        }
        return command.execute(request, manager, scanner);
    }
}