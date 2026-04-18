package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.Command;


/**
 * Команда для выполнения скрипта из файла.
 * @author Pwsha
 * @version v1.3
 */
public class ExecuteScriptCommand implements Command {
    @Override
    public CommandResponse execute(CommandRequest request) {
        return CommandResponse.error("execute_script выполняется на клиенте");
    }

    @Override
    public String getName() {
        return "execute_script";
    }

    @Override
    public String getDescription() {
        return "считать и исполнить скрипт из указанного файла";
    }

    @Override
    public String getSyntax() {
        return "execute_script file_name";
    }
}