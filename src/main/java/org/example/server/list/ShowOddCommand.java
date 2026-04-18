package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;
import org.example.server.Command;

/**
 * Класс команды отображения коллекции
 * @author Pwsha
 * @version v1.3
 */
public class ShowOddCommand implements Command {
    private final CollectionManager manager;

    public ShowOddCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        if (manager.getCollection().isEmpty()) {
            return CommandResponse.error("Коллекция пуста");
        }

        StringBuilder sb = new StringBuilder("Элементы коллекции:\n");
        manager.getCollection().stream()
                .filter(g -> g.getId()%2==0)
                .forEach(g -> sb.append("  ").append(g).append("\n"));
        return CommandResponse.success(sb.toString());
    }

    @Override
    public String getName() { return "show_odd"; }

    @Override
    public String getDescription() { return "показать все элементы с чётным id"; }

    @Override
    public String getSyntax() { return "show_odd"; }
}