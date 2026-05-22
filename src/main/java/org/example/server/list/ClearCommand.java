package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;

import java.util.Scanner;

public class ClearCommand implements Command {
    private final CollectionManager manager;

    public ClearCommand(CollectionManager manager) { this.manager = manager; }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId) {
        this.manager.getCollection().clear();
        return CommandResponse.success("Коллекция очищена");
    }

    @Override public String getName() { return "clear"; }
    @Override public String getDescription() { return "очистить коллекцию"; }
    @Override public String getSyntax() { return "clear"; }
}