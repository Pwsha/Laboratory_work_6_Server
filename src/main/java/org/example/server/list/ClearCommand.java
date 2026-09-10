package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;

import java.util.Scanner;

public class ClearCommand implements Command {
    private final CollectionManager manager;

    public ClearCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId) {
        boolean success = manager.clear(userId);
        if (success) {
            manager.refreshFromDatabase();
            return CommandResponse.success("Ваши объекты удалены");
        } else {
            return CommandResponse.error("Ошибка при очистке");
        }
    }

    @Override
    public String getName() { return "clear"; }
    @Override
    public String getDescription() { return "очистить коллекцию (только свои объекты)"; }
    @Override
    public String getSyntax() { return "clear"; }
}