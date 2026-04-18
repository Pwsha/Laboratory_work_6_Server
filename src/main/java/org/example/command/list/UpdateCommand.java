package org.example.command.list;

import org.example.command.Command;
import org.example.init.StudyGroup;
import org.example.program.*;

import java.util.HashSet;
import java.util.Scanner;

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
    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
        if (args.length == 0) {
            return "Ошибка: укажите id";
        }

        if (args.length > 2) {
            return "Ошибка: введено неверное количество аргументов.";
        }

        try {
            Long id = Long.parseLong(args[0]);

            StudyGroup existing = manager.getCollection().stream()
                    .filter(g -> g.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                return "Элемент с id " + id + " не найден";
            }

            StudyGroup newData;

            if (args.length == 1) {
                StudyGroupReader.setConsoleMode();

                System.out.println("Редактирование элемента с id: " + id);
                System.out.println("Текущие данные: " + existing);
                System.out.println("Введите новые данные:");
                newData = StudyGroupReader.read(manager.getCollection(), scanner);
            } else {
                StudyGroupReader.setScriptMode(args[1]);
                newData = StudyGroupReader.read(manager.getCollection(), scanner);
            }

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

            StudyGroupReader.reset();

            return "Элемент с id " + id + " успешно обновлен";

        } catch (NumberFormatException e) {
            return "Ошибка: id должен быть числом";
        }
    }

    @Override
    public String getName() { return "update"; }

    @Override
    public String getDescription() { return "обновить элемент по id"; }

    @Override
    public String getSyntax() { return "update id"; }
}