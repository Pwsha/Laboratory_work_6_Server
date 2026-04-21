package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.init.StudyGroup;
import org.example.server.Command;
import org.example.server.CollectionManager;
import org.example.server.StudyGroupReader;

import java.util.Scanner;

/**
 * Класс команды добавления объекта
 * @author Pwsha
 * @version v1.3
 */
public class AddCommand implements Command {
    private final CollectionManager manager;

    public AddCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner) {
        StudyGroup group = StudyGroupReader.read(manager.getCollection(), scanner);
        manager.add(group);
        return CommandResponse.success("Элемент добавлен с id: " + request.getStudyGroup().getId());
    }

    @Override
    public String getName() { return "add"; }

    @Override
    public String getDescription() { return "добавить новый элемент"; }

    @Override
    public String getSyntax() { return "add"; }
}