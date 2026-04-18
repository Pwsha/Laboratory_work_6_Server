package org.example.command.list;

import org.example.command.Command;
import org.example.init.StudyGroup;
import org.example.program.CollectionManager;

import java.util.HashSet;
import java.util.Scanner;

/**
 * Класс команды отображения коллекции
 * @author Pwsha
 * @version v1.3
 */
public class ShowOddCommand implements Command {
    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        if (collection.isEmpty()) {
            return "Коллекция пуста";
        }

        StringBuilder sb = new StringBuilder("Элементы коллекции:\n");
        collection.stream()
                .filter(g -> g.getId()%2==0)
                .forEach(g -> sb.append("  ").append(g).append("\n"));
        return sb.toString();
    }

    @Override
    public String getName() { return "show_odd"; }

    @Override
    public String getDescription() { return "показать все элементы с чётным id"; }

    @Override
    public String getSyntax() { return "show_odd"; }
}