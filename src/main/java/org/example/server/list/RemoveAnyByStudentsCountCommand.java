package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;

import java.util.Optional;
import java.util.Scanner;

public class RemoveAnyByStudentsCountCommand implements Command {
    private final CollectionManager manager;

    public RemoveAnyByStudentsCountCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId) {
        Long studentsCount = request.getStudentsCount();

        if (studentsCount == null) {
            return CommandResponse.error("Students count not specified");
        }

        Optional<StudyGroup> toRemove = manager.getCollection().stream()
                .filter(g -> g.getStudentsCount() == studentsCount && g.getUserId() == userId)
                .findFirst();

        if (toRemove.isPresent()) {
            Long id = toRemove.get().getId();
            boolean success = manager.removeById(id, userId);
            if (success) {
                return CommandResponse.success("Element with studentsCount " + studentsCount + " removed");
            } else {
                return CommandResponse.error("Database error");
            }
        } else {
            return CommandResponse.error("Element with studentsCount " + studentsCount + " not found");
        }
    }

    @Override
    public String getName() { return "remove_any_by_students_count"; }
    @Override
    public String getDescription() { return "удалить элемент по количеству студентов"; }
    @Override
    public String getSyntax() { return "remove_any_by_students_count studentsCount"; }
}