package org.example;

import org.example.program.*;

import java.util.Scanner;

/**
 * Главный класс приложения
 * @author Pwsha
 * @version v1.3
 */
public class App {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Ошибка: укажите имя файла");
            System.out.println("Пример: java -jar *.jar data.csv");
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);

        CollectionManager collectionManager = new CollectionManager(args[0]);
        CommandManager commandManager = new CommandManager(collectionManager, scanner);

        commandManager.run();
        scanner.close();
    }
}