package org.example.server;

import org.example.common.command.*;

import java.util.Scanner;

/**
 * Интерфейс для команд
 * @author Pwsha
 * @version v1.3
 */
public interface Command {

    /**
     * Метод запуска
     */
    CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner);

    /**
     * Функция получения значения имени
     */
    String getName();

    /**
     * Функция получения значения описания
     */
    String getDescription();

    /**
     * Функция получения значения синтаксиса
     */
    String getSyntax();
}