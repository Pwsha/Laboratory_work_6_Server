package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ShowCommand implements Command {
    private final CollectionManager manager;

    public ShowCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId) {
        if (manager.getCollection().isEmpty()) {
            return CommandResponse.success("Коллекция пуста");
        }

        List<StudyGroup> sorted = manager.getCollection().stream()
                .sorted()
                .collect(Collectors.toList());

        return CommandResponse.withCollection(
                "Элементы коллекции (всего: " + sorted.size() + "):",
                sorted);
    }

    @Override
    public String getName() { return "show"; }
    @Override
    public String getDescription() { return "показать все элементы"; }
    @Override
    public String getSyntax() { return "show"; }
}