package org.example.command.list;

import org.example.command.*;
import org.example.init.StudyGroup;
import org.example.program.*;

import java.util.HashSet;
import java.util.Scanner;

/**
 * Класс команды удаления всех элементов больше заданного
 * @author Pwsha
 * @version v1.3
 */
public class RemoveGreaterCommand implements Command {
    private final CollectionManager manager;

    public RemoveGreaterCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        if (args.length > 1) {
            return "Ошибка: введено неверное количество аргументов.";
        }

        if (manager.getCollection().isEmpty()) {
            return "Коллекция пуста";
        }

        StudyGroup reference = StudyGroupReader.read(manager.getCollection(), scanner);

        int initialSize = manager.getCollection().size();
        manager.getCollection().removeIf(group -> group.compareTo(reference) > 0);
        int removed = initialSize - manager.getCollection().size();

        if (removed == 0) {
            return "Нет элементов, превышающих заданный";
        } else {
            return "Удалено элементов: " + removed;
        }
    }

    @Override
    public String getName() { return "remove_greater {element}"; }

    @Override
    public String getDescription() { return "удалить элементы, превышающие заданный"; }

    @Override
    public String getSyntax() { return "remove_greater"; }
}