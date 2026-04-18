package org.example.command.list;

import org.example.command.Command;
import org.example.init.StudyGroup;
import org.example.program.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Класс команды вывода информации о коллекции.
 * @author Pwsha
 * @version v1.3
 */
public class InfoCommand implements Command {
    private final LocalDateTime initializationDate;
    private final CollectionManager manager;

    public InfoCommand(LocalDateTime initializationDate, CollectionManager manager) {
        this.initializationDate = initializationDate;
        this.manager = manager;
    }

    @Override
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        return String.format(
                "Тип коллекции: %s\nДата инициализации: %s\nКоличество элементов: %d",
                manager.getCollection().getClass().getSimpleName(),
                initializationDate,
                manager.getCollection().size()
        );
    }

    @Override
    public String getName() { return "info"; }

    @Override
    public String getDescription() { return "информация о коллекции"; }

    @Override
    public String getSyntax() { return "info"; }
}