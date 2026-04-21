package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;


import java.util.Scanner;

public class RemoveGreaterCommand implements Command {
    private final CollectionManager manager;

    public RemoveGreaterCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner) {
        StudyGroup reference = request.getStudyGroup();

        if (reference == null) {
            return CommandResponse.error("Не указан эталонный элемент");
        }

        if (manager.getCollection().isEmpty()) {
            return CommandResponse.success("Коллекция пуста");
        }

        int initialSize = manager.getCollection().size();
        manager.getCollection().removeIf(g -> g.compareTo(reference) > 0);
        int removed = initialSize - manager.getCollection().size();

        return CommandResponse.success("Удалено элементов: " + removed);
    }

    @Override
    public String getName() { return "remove_greater"; }
    @Override
    public String getDescription() { return "удалить элементы, превышающие заданный"; }
    @Override
    public String getSyntax() { return "remove_greater {element}"; }
}