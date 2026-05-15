package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;

import java.time.LocalDateTime;
import java.util.Scanner;

public class AddIfMaxCommand implements Command {
    private final CollectionManager manager;

    public AddIfMaxCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId) {
        StudyGroup group = request.getStudyGroup();

        if (group == null) {
            return CommandResponse.error("Не указан элемент");
        }

        group.setCreationDate(LocalDateTime.now());

        StudyGroup max = manager.getCollection().stream()
                .max(StudyGroup::compareTo)
                .orElse(null);

        if (max == null || group.compareTo(max) > 0) {
            boolean success = manager.add(group, userId);
            if (success) {
                return CommandResponse.success("Элемент добавлен" + (max == null ? " (коллекция была пуста)" : " (превышает максимальный)"));
            } else {
                return CommandResponse.error("Ошибка при добавлении в БД");
            }
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