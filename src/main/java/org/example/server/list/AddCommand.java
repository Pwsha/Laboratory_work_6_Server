package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;
import org.example.client.CommandHelper;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Scanner;

public class AddCommand implements Command {
    private final CollectionManager manager;

    public AddCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId) {
        StudyGroup group = request.getStudyGroup();

        if (group == null) {
            return CommandResponse.error("Не указан элемент для добавления");
        }

        group.setCreationDate(LocalDateTime.now());
        boolean success = manager.add(group, userId);

        if (success) {
            return CommandResponse.success("Элемент добавлен с id: " + group.getId());
        } else {
            return CommandResponse.error("Ошибка при добавлении в БД");
        }
    }

    @Override
    public String getName() { return "add"; }
    @Override
    public String getDescription() { return "добавить новый элемент"; }
    @Override
    public String getSyntax() { return "add {element}"; }
}