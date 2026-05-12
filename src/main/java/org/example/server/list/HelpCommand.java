package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.server.CollectionManager;

import java.util.Map;
import java.util.Scanner;

/**
 * Класс команды help
 * @author Pwsha
 * @version v1.3
 */
public class HelpCommand implements Command {
    private final Map<CommandType, Command> commands;

    public HelpCommand(Map<CommandType, Command> commands) {
        this.commands = commands;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner){
        StringBuilder sb = new StringBuilder("Доступные команды:\n");
        commands.values().stream()
                .sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
                .forEach(cmd -> sb.append(String.format("  %-30s - %s\n", cmd.getSyntax(), cmd.getDescription())));
        return CommandResponse.success(sb.toString());
    }

    @Override
    public String getName() { return "help"; }

    @Override
    public String getDescription() { return "вывести справку"; }

    @Override
    public String getSyntax() { return "help"; }
}