package org.example.command.list;

import org.example.command.Command;
import org.example.init.StudyGroup;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

/**
 * Класс команды help
 * @author Pwsha
 * @version v1.3
 */
public class HelpCommand implements Command {
    private final Map<String, Command> commands;

    public HelpCommand(Map<String, Command> commands) {
        this.commands = commands;
    }

    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        StringBuilder sb = new StringBuilder("Доступные команды:\n");
        commands.values().stream()
                .sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
                .forEach(cmd -> sb.append(String.format("  %-30s - %s\n", cmd.getSyntax(), cmd.getDescription())));
        return sb.toString();
    }

    @Override
    public String getName() { return "help"; }

    @Override
    public String getDescription() { return "вывести справку"; }

    @Override
    public String getSyntax() { return "help"; }
}