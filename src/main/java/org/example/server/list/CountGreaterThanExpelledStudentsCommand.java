package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;

import java.util.Scanner;

public class CountGreaterThanExpelledStudentsCommand implements Command {
    private final CollectionManager manager;

    public CountGreaterThanExpelledStudentsCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId) {
        Integer expelledStudents = request.getExpelledStudents();

        if (expelledStudents == null) {
            return CommandResponse.error("Не указано количество отчисленных");
        }

        long count = manager.getCollection().stream()
                .filter(g -> g.getExpelledStudents() > expelledStudents)
                .count();

        return CommandResponse.withCount(
                "Количество элементов с expelledStudents > " + expelledStudents + ": " + count, count);
    }

    @Override
    public String getName() { return "count_greater_than_expelled_students"; }
    @Override
    public String getDescription() { return "количество элементов с expelledStudents > заданного"; }
    @Override
    public String getSyntax() { return "count_greater_than_expelled_students expelledStudents"; }
}