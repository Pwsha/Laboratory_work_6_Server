package org.example.command.list;

import org.example.command.Command;
import org.example.init.StudyGroup;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Класс команды для поиска объекта с минимальным семестром
 * @author Pwsha
 * @version v1.3
 */
public class MinBySemesterEnumCommand implements Command {

    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        if (collection.isEmpty()) {
            return "Коллекция пуста";
        }

        StudyGroup min = collection.stream()
                .min(Comparator.comparing(StudyGroup::getSemesterEnum))
                .orElse(null);

        return min != null ?
                "Элемент с минимальным semesterEnum:\n  " + min :
                "Не удалось найти элемент";
    }

    @Override
    public String getName() { return "min_by_semester_enum"; }

    @Override
    public String getDescription() { return "элемент с минимальным semesterEnum"; }

    @Override
    public String getSyntax() { return "min_by_semester_enum"; }
}