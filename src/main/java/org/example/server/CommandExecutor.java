package org.example.server;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.server.list.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class CommandExecutor {
    private final Map<CommandType, Command> commands = new HashMap<>();
    private final CollectionManager manager;
    private final Scanner scanner;
    private final AuthManager authManager;
    private final ForkJoinPool forkJoinPool;

    public CommandExecutor(CollectionManager manager, AuthManager authManager, Scanner scanner) {
        this.manager = manager;
        this.authManager = authManager;
        this.scanner = scanner;
        this.forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        initCommands();
    }

    private void initCommands() {
        commands.put(CommandType.INFO, new InfoCommand(manager));
        commands.put(CommandType.SHOW, new ShowCommand(manager));
        commands.put(CommandType.SHOW_ODD, new ShowOddCommand(manager));
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
        if (!authManager.isAuthorized(request)) {
            return CommandResponse.error("Необходимо авторизоваться. Используйте login");
        }

        CommandType type = request.getType();

        if (type == CommandType.LOGIN) {
            return authManager.handleLogin(request);
        }
        if (type == CommandType.REGISTER) {
            return authManager.handleRegister(request);
        }
        if (type == CommandType.LOGOUT) {
            return authManager.handleLogout(request);
        }

        // Получаем userId для проверки прав
        java.util.Optional<Integer> userIdOpt = authManager.getUserId(request);
        if (userIdOpt.isEmpty()) {
            return CommandResponse.error("Сессия не найдена. Выполните login");
        }
        int userId = userIdOpt.get();

        Command command = commands.get(type);
        if (command == null) {
            return CommandResponse.error("Неизвестная команда");
        }

        ForkJoinTask<CommandResponse> task = forkJoinPool.submit(() ->
                command.execute(request, manager, scanner, userId)
        );

        try {
            return task.get();
        } catch (Exception e) {
            return CommandResponse.error("Ошибка выполнения: " + e.getMessage());
        }
    }

    public void shutdown() {
        forkJoinPool.shutdown();
    }
}