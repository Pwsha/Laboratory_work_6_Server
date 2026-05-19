package org.example.client;

import org.example.common.command.CommandType;

import java.util.LinkedList;

public class History {
    private final LinkedList<String> history = new LinkedList<>();

    public void add(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        String[] parts = command.trim().split("\\s+");
        String commandName = parts[0];

        CommandType type = CommandType.fromString(commandName);
        if (type == null) {
            return;
        }

        history.add(commandName);
        if (history.size() > 5) {
            history.removeFirst();
        }
    }

    public void print_history() {
        if (history.isEmpty()) {
            System.out.println("История команд пуста");
        } else {
            System.out.println("Последние команды:");
            history.forEach(cmd -> System.out.println("  " + cmd));
        }
    }
}