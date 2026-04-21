package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;
import org.example.server.Command;

import java.util.Scanner;

/**
 * Класс команды очищения коллекции
 * @author Pwsha
 * @version v1.3
 */
public class ClearCommand implements Command {
    private final CollectionManager manager;

    public ClearCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner){
        int size = manager.getCollection().size();
        manager.clear();
        manager.saveCollection();
        return CommandResponse.success("Коллекция очищена. Удалено элементов: " + size);
    }

    @Override
    public String getName() { return "clear"; }

    @Override
    public String getDescription() { return "очистить коллекцию"; }

    @Override
    public String getSyntax() { return "clear"; }
}