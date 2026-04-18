package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.Command;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;

/**
 * Класс команды обновления элемента по id
 * @author Pwsha
 * @version v1.3
 */
public class UpdateCommand implements Command {
    private final CollectionManager manager;

    public UpdateCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        Long id = request.getId();
        StudyGroup newData = request.getStudyGroup();

        if (id == null || newData == null) {
            return CommandResponse.error("Не указаны id или новый элемент");
        }

        try {
            StudyGroup existing = manager.getCollection().stream()
                    .filter(g -> g.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                return CommandResponse.error("Элемент с id " + id + " не найден");
            }

            System.out.println("Редактирование элемента с id: " + id);
            newData.setId(id);
            newData.setCreationDate(existing.getCreationDate());

            StudyGroup updatedGroup = new StudyGroup.Builder()
                    .id(id)
                    .name(newData.getName())
                    .coordinates(newData.getCoordinates())
                    .creationDate(existing.getCreationDate())
                    .studentsCount(newData.getStudentsCount())
                    .expelledStudents(newData.getExpelledStudents())
                    .formOfEducation(newData.getFormOfEducation())
                    .semesterEnum(newData.getSemesterEnum())
                    .groupAdmin(newData.getGroupAdmin())
                    .build();

            manager.removeById(id);
            manager.add(updatedGroup);

            return CommandResponse.success("Элемент с id " + id + " успешно обновлен");

        } catch (NumberFormatException e) {
            return CommandResponse.error("Ошибка: id должен быть числом");
        }
    }

    @Override
    public String getName() { return "update"; }

    @Override
    public String getDescription() { return "обновить элемент по id"; }

    @Override
    public String getSyntax() { return "update id"; }
}