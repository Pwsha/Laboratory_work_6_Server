package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;

import java.util.Scanner;

public class RemoveByIdCommand implements Command {
    private final CollectionManager manager;

    public RemoveByIdCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId) {
        Long id = request.getId();
        if (id == null) {
            return CommandResponse.error("Не указан id");
        }

        // Проверяем, существует ли объект и принадлежит ли пользователю
        StudyGroup group = manager.getCollection().stream()
                .filter(g -> g.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (group == null) {
            return CommandResponse.error("Элемент с id " + id + " не найден");
        }

        // Проверка прав: только владелец может удалять
        if (group.getUserId() != null && group.getUserId() != userId) {
            System.out.println("DEBUG: User " + userId + " tried to delete group owned by " + group.getUserId());
            return CommandResponse.error("Вы можете удалять только свои объекты");
        }

        boolean success = manager.removeById(id, userId);

        if (success) {
            return CommandResponse.success("Элемент с id " + id + " удалён");
        } else {
            return CommandResponse.error("Элемент с id " + id + " не найден");
        }
    }

    @Override
    public String getName() { return "remove_by_id"; }
    @Override
    public String getDescription() { return "удалить элемент по id"; }
    @Override
    public String getSyntax() { return "remove_by_id id"; }
}