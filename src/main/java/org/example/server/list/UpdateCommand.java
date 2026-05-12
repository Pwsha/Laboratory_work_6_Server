package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;

import java.util.Scanner;

public class UpdateCommand implements Command {
    private final CollectionManager manager;

    public UpdateCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId) {
        Long id = request.getId();
        StudyGroup newGroup = request.getStudyGroup();

        if (id == null || newGroup == null) {
            return CommandResponse.error("Не указаны id или новый элемент");
        }

        StudyGroup existing = manager.getCollection().stream()
                .filter(g -> g.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (existing == null) {
            return CommandResponse.error("Элемент с id " + id + " не найден");
        }

        newGroup.setId(id);
        newGroup.setCreationDate(existing.getCreationDate());

        manager.removeById(id, userId);
        manager.add(newGroup, userId);

        return CommandResponse.success("Элемент с id " + id + " обновлён");
    }

    @Override
    public String getName() { return "update"; }
    @Override
    public String getDescription() { return "обновить элемент по id"; }
    @Override
    public String getSyntax() { return "update id {element}"; }
}