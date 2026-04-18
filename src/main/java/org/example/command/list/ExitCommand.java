package org.example.command.list;

import org.example.command.Command;
import org.example.init.StudyGroup;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Класс команды выхода из приложения
 * @author Pwsha
 * @version v1.3
 */
public class ExitCommand implements Command {

    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        System.out.println("Завершение работы...");
        System.exit(0);
        return "exit";
    }

    @Override
    public String getName() { return "exit"; }

    @Override
    public String getDescription() { return "завершить программу"; }

    @Override
    public String getSyntax() { return "exit"; }
}