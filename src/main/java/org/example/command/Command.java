package org.example.command;

import org.example.init.StudyGroup;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Интерфейс для команд
 * @author Pwsha
 * @version v1.3
 */
public interface Command {

    /**
     * Метод запуска
     * @param args
     * @param collection
     * @param scanner
     */
    String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner);

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