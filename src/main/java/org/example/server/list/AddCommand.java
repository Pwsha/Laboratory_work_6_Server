package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;
import org.example.client.CommandHelper;

import java.time.LocalDateTime;
import java.util.Scanner;

public class AddCommand implements Command {
    private final CollectionManager manager;

    public AddCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner) {
        StudyGroup group = request.getStudyGroup();

        if (group == null) {
            return CommandResponse.error("Не указан элемент для добавления");
        }

        Long newId = CommandHelper.generateId(manager.getCollection());
        group.setId(newId);
        group.setCreationDate(LocalDateTime.now());

        manager.add(group);

        return CommandResponse.success("Элемент добавлен с id: " + group.getId());
    }

    @Override
    public String getName() { return "add"; }
    @Override
    public String getDescription() { return "добавить новый элемент"; }
    @Override
    public String getSyntax() { return "add {element}"; }
}