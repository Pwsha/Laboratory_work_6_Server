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
        List<StudyGroup> groups = this.manager.getCollection().stream()
                .filter(g -> g != null && g.getId() != null)  // ← фильтруем null
                .sorted()
                .collect(Collectors.toList());

        // Убеждаемся, что id установлен (на всякий случай)
        for (StudyGroup g : groups) {
            if (g.getId() == null) {
                System.err.println("WARNING: StudyGroup with null id detected: " + g.getName());
            }
        }

        return CommandResponse.withCollection("Элементы коллекции (всего: " + groups.size() + "):", groups);
    }

    @Override
    public String getName() { return "show"; }
    @Override
    public String getDescription() { return "показать все элементы"; }
    @Override
    public String getSyntax() { return "show"; }
}