package org.example.program;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.example.command.Command;
import org.example.command.list.AddCommand;
import org.example.command.list.AddIfMaxCommand;
import org.example.command.list.ClearCommand;
import org.example.command.list.CountGreaterThanExpelledStudentsCommand;
import org.example.command.list.ExecuteScriptCommand;
import org.example.command.list.ExitCommand;
import org.example.command.list.HelpCommand;
import org.example.command.list.HistoryCommand;
import org.example.command.list.InfoCommand;
import org.example.command.list.MinBySemesterEnumCommand;
import org.example.command.list.RemoveAnyByStudentsCountCommand;
import org.example.command.list.RemoveByIdCommand;
import org.example.command.list.RemoveGreaterCommand;
import org.example.command.list.SaveCommand;
import org.example.command.list.ShowCommand;
import org.example.command.list.ShowOddCommand;
import org.example.command.list.UpdateCommand;

/**
 * Класс управления командами приложения
 * @author Pwsha
 * @version v1.3
 */
public class CommandManager {
    private final Map<String, Command> commands;
    private final LinkedList<String> commandHistory;
    private final LocalDateTime startTime;
    private final CollectionManager collectionManager;
    private final Scanner scanner;

    public CommandManager(CollectionManager collectionManager, Scanner scanner) {
        this.commands = new HashMap<>();
        this.commandHistory = new LinkedList<>();
        this.startTime = LocalDateTime.now();
        this.collectionManager = collectionManager;
        this.scanner = scanner;

        initializeCommands();
    }

    private void initializeCommands() {
        commands.put("help", new HelpCommand(commands));
        commands.put("info", new InfoCommand(startTime, collectionManager));
        commands.put("show", new ShowCommand());
        commands.put("show_odd", new ShowOddCommand());
        commands.put("add", new AddCommand(collectionManager));
        commands.put("update", new UpdateCommand(collectionManager));
        commands.put("remove_by_id", new RemoveByIdCommand());
        commands.put("clear", new ClearCommand());
        commands.put("save", new SaveCommand(collectionManager));
        commands.put("exit", new ExitCommand());
        commands.put("add_if_max", new AddIfMaxCommand(collectionManager));
        commands.put("remove_greater", new RemoveGreaterCommand(collectionManager));
        commands.put("history", new HistoryCommand(commandHistory));
        commands.put("remove_any_by_students_count", new RemoveAnyByStudentsCountCommand());
        commands.put("min_by_semester_enum", new MinBySemesterEnumCommand());
        commands.put("count_greater_than_expelled_students", new CountGreaterThanExpelledStudentsCommand());
        commands.put("execute_script", new ExecuteScriptCommand(commands));
    }

    public void run() {
        System.out.println("Программа запущена. Введите 'help' для справки.");

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

            addToHistory(cmdName);

            Command command = commands.get(cmdName);
            if (command == null) {
                System.out.println("Неизвестная команда. Введите 'help'");
                continue;
            }

            try {
                if (cmdArgs.isEmpty()) {
                    StudyGroupReader.setConsoleMode();
                } else {
                    StudyGroupReader.setScriptMode(cmdArgs);
                }

                String[] argArray = cmdArgs.isEmpty() ? new String[0] : new String[]{cmdArgs};
                String result = command.execute(argArray, collectionManager.getCollection(), scanner);
                System.out.println(result);

                StudyGroupReader.reset();

            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
                StudyGroupReader.reset();
            }
        }
    }

    private void addToHistory(String command) {
        if (commands.containsKey(command)) {
            commandHistory.add(command);
            if (commandHistory.size() > 5) {
                commandHistory.removeFirst();
            }
        }
    }
}