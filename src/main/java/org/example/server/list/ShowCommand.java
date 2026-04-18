package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;
import org.example.server.Command;
import org.example.common.init.StudyGroup;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Класс команды отображения коллекции
 * @author Pwsha
 * @version v1.3
 */
public class ShowCommand implements Command {
    private final CollectionManager manager;

    public ShowCommand(CollectionManager manager){
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        if (manager.getCollection().isEmpty()) {
            return CommandResponse.success("Коллекция пуста");
        }

        List<StudyGroup> show = manager.getCollection().stream()
                .sorted()
                .collect(Collectors.toList());
        return CommandResponse.withCollection("Элементы коллекции:\n", show);
    }

    @Override
    public String getName() { return "show"; }

    @Override
    public String getDescription() { return "показать все элементы"; }

    @Override
    public String getSyntax() { return "show"; }
}