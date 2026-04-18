package org.example.command.list;

import org.example.command.Command;
import org.example.init.StudyGroup;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Класс команды подсчёта отчисленных студентов
 * @author Pwsha
 * @version v1.3
 */
public class CountGreaterThanExpelledStudentsCommand implements Command {

    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        if (args.length == 0) {
            return "Ошибка: укажите expelledStudents";
        } else if (args.length > 1) {
            return "Ошибка: указано больше одного аргумента";
        }

        try {
            int value = Integer.parseInt(args[0]);
            long count = collection.stream()
                    .filter(g -> g.getExpelledStudents() > value)
                    .count();

            return "Количество элементов с expelledStudents > " + value + ": " + count;
        } catch (NumberFormatException e) {
            return "Ошибка: expelledStudents должен быть числом";
        }
    }

    @Override
    public String getName() { return "count_greater_than_expelled_students"; }

    @Override
    public String getDescription() { return "количество элементов с expelledStudents > заданного"; }

    @Override
    public String getSyntax() { return "count_greater_than_expelled_students expelledStudents"; }
}