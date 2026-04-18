package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.Command;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;

/**
 * Класс команды добавления элемента если он больше других
 * @author Pwsha
 * @version v1.3
 */
public class AddIfMaxCommand implements Command {
    private final CollectionManager manager;

    public AddIfMaxCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        StudyGroup group = request.getStudyGroup();

        if (group == null) {
            return CommandResponse.error("Не указан элемент");
        }

        if (manager.getCollection().isEmpty()) {
            manager.add(group);
            return CommandResponse.success("Элемент добавлен (коллекция была пуста)");
        }

        StudyGroup max = manager.getCollection().stream()
                .max(StudyGroup::compareTo)
                .orElse(null);

        if (group.compareTo(max) > 0) {
            manager.add(group);
            return CommandResponse.success("Элемент добавлен");
        }

        return CommandResponse.success("Элемент не добавлен (не превышает максимальный)");
    }

    @Override
    public String getName() { return "add_if_max {element}"; }

    @Override
    public String getDescription() { return "добавить элемент, если он превышает максимальный"; }

    @Override
    public String getSyntax() { return "add_if_max"; }
}