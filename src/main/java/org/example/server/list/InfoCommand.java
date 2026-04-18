package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.Command;
import org.example.server.CollectionManager;

import java.time.LocalDateTime;

/**
 * Класс команды вывода информации о коллекции.
 * @author Pwsha
 * @version v1.3
 */
public class InfoCommand implements Command {
    private final LocalDateTime initializationDate;
    private final CollectionManager manager;

    public InfoCommand(LocalDateTime initializationDate, CollectionManager manager) {
        this.initializationDate = initializationDate;
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        String info = String.format(
                "Тип коллекции: %s\nДата инициализации: %s\nКоличество элементов: %d",
                manager.getCollection().getClass().getSimpleName(),
                initializationDate,
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