package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.Command;

/**
 * Класс команды history
 * @author Pwsha
 * @version v1.3
 */
public class HistoryCommand implements Command {
    @Override
    public CommandResponse execute(CommandRequest request) {
        return CommandResponse.success("История команд доступна только на клиенте");
    }

    @Override
    public String getName() { return "history"; }

    @Override
    public String getDescription() { return "вывести историю команд"; }

    @Override
    public String getSyntax() { return "history"; }
}