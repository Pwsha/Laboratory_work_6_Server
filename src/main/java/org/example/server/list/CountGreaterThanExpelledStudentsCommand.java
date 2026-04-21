package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;
import org.example.server.Command;

import java.util.Scanner;

/**
 * Класс команды подсчёта отчисленных студентов
 * @author Pwsha
 * @version v1.3
 */
public class CountGreaterThanExpelledStudentsCommand implements Command {
    private final CollectionManager manager;

    public CountGreaterThanExpelledStudentsCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner){
        Integer expelledStudents = request.getExpelledStudents();

        if (expelledStudents == null) {
            return CommandResponse.error("Не указано количество отчисленных");
        }

        try {
            long count = manager.getCollection().stream()
                    .filter(g -> g.getExpelledStudents() > expelledStudents)
                    .count();

            return CommandResponse.success("Количество элементов с expelledStudents > " + expelledStudents + ": " + count);
        } catch (NumberFormatException e) {
            return CommandResponse.error("Ошибка: expelledStudents должен быть числом");
        }
    }

    @Override
    public String getName() { return "count_greater_than_expelled_students"; }

    @Override
    public String getDescription() { return "количество элементов с expelledStudents > заданного"; }

    @Override
    public String getSyntax() { return "count_greater_than_expelled_students expelledStudents"; }
}