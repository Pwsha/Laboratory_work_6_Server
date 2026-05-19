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
            return CommandResponse.error("Не указано количество студентов");
        }

        Optional<StudyGroup> toRemove = manager.getCollection().stream()
                .filter(g -> g.getStudentsCount() == studentsCount)
                .findFirst();

        if (toRemove.isPresent()) {
            manager.removeById(toRemove.get().getId(), userId);
            return CommandResponse.success("Элемент с studentsCount=" + studentsCount + " удалён");
        }

        return CommandResponse.error("Элемент с studentsCount=" + studentsCount + " не найден");
    }

    @Override
    public String getName() { return "remove_any_by_students_count"; }
    @Override
    public String getDescription() { return "удалить элемент по studentsCount"; }
    @Override
    public String getSyntax() { return "remove_any_by_students_count studentsCount"; }
}