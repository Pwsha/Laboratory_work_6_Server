package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;
import org.example.server.Command;
import org.example.common.init.StudyGroup;
import java.util.Optional;

/**
 * Команда для удаления элемента по количеству студентов.
 * @author Pwsha
 * @version v1.3
 */
public class RemoveAnyByStudentsCountCommand implements Command {
    private final CollectionManager manager;

    public RemoveAnyByStudentsCountCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        Long studentsCount = request.getStudentsCount();

        if (studentsCount == null) {
            return CommandResponse.error("Не указано количество студентов");
        }

        try {
            Optional<StudyGroup> remove = manager.getCollection().stream()
                    .filter(g -> g.getStudentsCount() == studentsCount)
                    .findFirst();

            if (remove.isPresent()) {
                manager.removeById(remove.get().getId());
                return CommandResponse.success("Элемент с studentsCount=" + studentsCount + " удален");
            } else {
                return CommandResponse.success("Элемент с studentsCount=" + studentsCount + " не найден");
            }
        } catch (NumberFormatException e) {
            return CommandResponse.error("Ошибка: studentsCount должен быть числом");
        }
    }

    @Override
    public String getName() { return "remove_any_by_students_count"; }

    @Override
    public String getDescription() { return "удалить элемент по studentsCount"; }

    @Override
    public String getSyntax() { return "remove_any_by_students_count studentsCount"; }
}