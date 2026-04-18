package org.example.command.list;

import org.example.command.*;
import org.example.init.StudyGroup;
import org.example.program.*;

import java.util.HashSet;
import java.util.Scanner;

/**
 * Класс команды добавления элемента если он больше других
 * @author Pwsha
 * @version v1.3
 */
public class AddIfMaxCommand implements Command {
    private final CollectionManager manager;

    public AddIfMaxCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        if (args.length > 1) {
            return "Ошибка: введено неверное количество аргументов.";
        }

        StudyGroup group = StudyGroupReader.read(manager.getCollection(), scanner);

        if (manager.getCollection().isEmpty()) {
            manager.add(group);
            return "Элемент добавлен (коллекция была пуста) с id: " + group.getId();
        }

        StudyGroup max = manager.getCollection().stream()
                .max(StudyGroup::compareTo)
                .orElse(null);

        if (group.compareTo(max) > 0) {
            manager.add(group);
            return "Элемент добавлен (превышает максимальный) с id: " + group.getId();
        }

        return "Элемент не добавлен (не превышает максимальный)";
    }

    @Override
    public String getName() { return "add_if_max {element}"; }

    @Override
    public String getDescription() { return "добавить элемент, если он превышает максимальный"; }

    @Override
    public String getSyntax() { return "add_if_max"; }
}