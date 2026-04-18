package org.example.command.list;

import org.example.command.Command;
import org.example.init.StudyGroup;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Класс команды history
 * @author Pwsha
 * @version v1.3
 */
public class HistoryCommand implements Command {
    private final LinkedList<String> history;

    public HistoryCommand(LinkedList<String> history) {
        this.history = history;
    }

    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        if (history.isEmpty()) {
            return "История команд пуста";
        }

        StringBuilder sb = new StringBuilder("Последние команды:\n");
        history.forEach(cmd -> sb.append("  ").append(cmd).append("\n"));
        return sb.toString();
    }

    @Override
    public String getName() { return "history"; }

    @Override
    public String getDescription() { return "вывести историю команд"; }

    @Override
    public String getSyntax() { return "history"; }
}