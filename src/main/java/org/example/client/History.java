package org.example.client;

import java.util.LinkedList;

public class History {
    private final LinkedList<String> history = new LinkedList<>();

    public void add(String command) {
        String[] parts = command.split("\\s+");
        String commandName = parts[0];

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