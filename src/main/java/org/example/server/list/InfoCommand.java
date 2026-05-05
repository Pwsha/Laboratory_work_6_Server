package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;

import java.time.LocalDateTime;
import java.util.Scanner;

public class InfoCommand implements Command {
    private final CollectionManager manager;
    private final LocalDateTime startTime = LocalDateTime.now();

    public InfoCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner) {
        String info = String.format(
                "Тип коллекции: %s\nДата инициализации: %s\nКоличество элементов: %d",
                manager.getCollection().getClass().getSimpleName(),
                startTime,
                manager.getCollection().size()
        );
        return CommandResponse.success(info);
    }

    @Override
    public String getName() { return "info"; }
    @Override
    public String getDescription() { return "информация о коллекции"; }
    @Override
    public String getSyntax() { return "info"; }
}