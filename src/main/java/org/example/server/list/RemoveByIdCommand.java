package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
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

        boolean removed = manager.removeById(id, userId);

        if (removed) {
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