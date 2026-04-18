package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;
import org.example.server.Command;
import org.example.common.init.StudyGroup;

import java.util.Comparator;

/**
 * Класс команды для поиска объекта с минимальным семестром
 * @author Pwsha
 * @version v1.3
 */
public class MinBySemesterEnumCommand implements Command {
    private final CollectionManager manager;

    public MinBySemesterEnumCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        if (manager.getCollection().isEmpty()) {
            return CommandResponse.error("Коллекция пуста");
        }

        StudyGroup min = manager.getCollection().stream()
                .min(Comparator.comparing(StudyGroup::getSemesterEnum))
                .orElse(null);

        if (min != null) {
            return CommandResponse.withGroup("Элемент с минимальным semesterEnum:", min);
        } else {
            return CommandResponse.error("Элемент не найден");
        }
    }

    @Override
    public String getName() { return "min_by_semester_enum"; }

    @Override
    public String getDescription() { return "элемент с минимальным semesterEnum"; }

    @Override
    public String getSyntax() { return "min_by_semester_enum"; }
}