package org.example.command.list;

import org.example.command.Command;
import org.example.init.StudyGroup;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Класс команды удаления по id
 * @author Pwsha
 * @version v1.3
 */
public class RemoveByIdCommand implements Command {

    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        if (args.length == 0) {
            return "Ошибка: укажите id";
        } else if (args.length > 1) {
            return "Ошибка: указано больше одного аргумента";
        }

        try {
            Long id = Long.parseLong(args[0]);
            boolean removed = collection.removeIf(g -> g.getId().equals(id));
            return removed ? "Элемент с id " + id + " удален" : "Элемент с id " + id + " не найден";
        } catch (NumberFormatException e) {
            return "Ошибка: id должен быть числом";
        }
    }

    @Override
    public String getName() { return "remove_by_id"; }

    @Override
    public String getDescription() { return "удалить элемент по id"; }

    @Override
    public String getSyntax() { return "remove_by_id id"; }
}