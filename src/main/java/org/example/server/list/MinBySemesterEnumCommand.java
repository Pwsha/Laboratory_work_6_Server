package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.init.StudyGroup;
import org.example.server.CollectionManager;

import java.util.Comparator;
import java.util.Scanner;

public class MinBySemesterEnumCommand implements Command {
    private final CollectionManager manager;

    public MinBySemesterEnumCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId) {
        return manager.getCollection().stream()
                .min(Comparator.comparing(StudyGroup::getSemesterEnum))
                .map(min -> CommandResponse.withGroup("Элемент с минимальным semesterEnum:", min))
                .orElse(CommandResponse.error("Коллекция пуста"));
    }

    @Override
    public String getName() { return "min_by_semester_enum"; }
    @Override
    public String getDescription() { return "элемент с минимальным semesterEnum"; }
    @Override
    public String getSyntax() { return "min_by_semester_enum"; }
}