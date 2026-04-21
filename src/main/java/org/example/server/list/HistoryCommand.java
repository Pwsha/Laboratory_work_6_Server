package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;

import java.util.Scanner;

public class HistoryCommand implements Command {

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner) {
        return CommandResponse.success("История команд доступна только на клиенте");
    }

    @Override
    public String getName() { return "history"; }
    @Override
    public String getDescription() { return "вывести историю команд"; }
    @Override
    public String getSyntax() { return "history"; }
}