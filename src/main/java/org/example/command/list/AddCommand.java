package org.example.command.list;

import org.example.command.*;
import org.example.init.StudyGroup;
import org.example.program.*;

import java.util.HashSet;
import java.util.Scanner;

/**
 * Класс команды добавления объекта
 * @author Pwsha
 * @version v1.3
 */
public class AddCommand implements Command {
    private final CollectionManager manager;

    public AddCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        if (args.length > 1) {
            return "Ошибка: введено неверное количество аргументов.";
        }

        StudyGroup group = StudyGroupReader.read(manager.getCollection(), scanner);

        manager.add(group);
        return "Элемент успешно добавлен с id: " + group.getId();
    }

    @Override
    public String getName() { return "add"; }

    @Override
    public String getDescription() { return "добавить новый элемент"; }

    @Override
    public String getSyntax() { return "add"; }
}