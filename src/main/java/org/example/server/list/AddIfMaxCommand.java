package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;
import org.example.client.CommandHelper;

import java.time.LocalDateTime;
import java.util.Scanner;

public class AddIfMaxCommand implements Command {
    private final CollectionManager manager;

    public AddIfMaxCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner) {
        StudyGroup group = request.getStudyGroup();

        if (group == null) {
            return CommandResponse.error("Не указан элемент");
        }

        if (manager.getCollection().isEmpty()) {
            Long newId = CommandHelper.generateId(manager.getCollection());
            group.setId(newId);
            group.setCreationDate(LocalDateTime.now());
            manager.add(group);
            return CommandResponse.success("Элемент добавлен (коллекция была пуста)");
        }

        StudyGroup max = manager.getCollection().stream()
                .max(StudyGroup::compareTo)
                .orElse(null);

        if (group.compareTo(max) > 0) {
            Long newId = CommandHelper.generateId(manager.getCollection());
            group.setId(newId);
            group.setCreationDate(LocalDateTime.now());
            manager.add(group);
            return CommandResponse.success("Элемент добавлен (превышает максимальный)");
        }

        return CommandResponse.success("Элемент не добавлен (не превышает максимальный)");
    }

    @Override
    public String getName() { return "add_if_max"; }
    @Override
    public String getDescription() { return "добавить элемент, если он превышает максимальный"; }
    @Override
    public String getSyntax() { return "add_if_max {element}"; }
}