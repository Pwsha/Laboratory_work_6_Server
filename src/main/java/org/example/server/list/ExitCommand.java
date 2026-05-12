package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;

import java.util.Scanner;

/**
 * Класс команды выхода из приложения
 * @author Pwsha
 * @version v1.3
 */
public class ExitCommand implements Command {
    private final CollectionManager manager;

    public ExitCommand (CollectionManager manager){
        this.manager = manager;
    }
    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId){
        System.out.println("Завершение работы...");
        System.exit(0);
        return CommandResponse.success("exit");
    }

    @Override
    public String getName() { return "exit"; }

    @Override
    public String getDescription() { return "завершить программу"; }

    @Override
    public String getSyntax() { return "exit"; }
}