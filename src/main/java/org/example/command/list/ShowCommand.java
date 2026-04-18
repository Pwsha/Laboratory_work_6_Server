package org.example.command.list;

import org.example.command.Command;
import org.example.init.StudyGroup;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Класс команды отображения коллекции
 * @author Pwsha
 * @version v1.3
 */
public class ShowCommand implements Command {

    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        if (collection.isEmpty()) {
            return "Коллекция пуста";
        }

        StringBuilder sb = new StringBuilder("Элементы коллекции:\n");
        collection.stream()
                .sorted()
                .forEach(g -> sb.append("  ").append(g).append("\n"));
        return sb.toString();
    }

    @Override
    public String getName() { return "show"; }

    @Override
    public String getDescription() { return "показать все элементы"; }

    @Override
    public String getSyntax() { return "show"; }
}