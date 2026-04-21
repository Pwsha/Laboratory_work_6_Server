package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;

import java.util.Scanner;

/**
 * Класс команды history
 * @author Pwsha
 * @version v1.3
 */
public class ExecuteScriptCommand implements Command {
    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner){
        return CommandResponse.success("Скрипт доступен только на клиенте");
    }

    @Override
    public String getName() { return "execute_script"; }

    @Override
    public String getDescription() { return "выполнить скрипт"; }

    @Override
    public String getSyntax() { return "execute_script file"; }
}