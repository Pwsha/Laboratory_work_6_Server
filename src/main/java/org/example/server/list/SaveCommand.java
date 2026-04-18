//package org.example.server.list;
//
//import org.example.server.Command;
//import org.example.common.init.StudyGroup;
//import org.example.server.CollectionManager;
//
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Scanner;
//
///**
// * Класс команды сохранения файла
// * @author Pwsha
// * @version v1.3
// */
//public class SaveCommand implements Command {
//    private final CollectionManager manager;
//
//    public SaveCommand(CollectionManager manager) {
//        this.manager = manager;
//    }
//
//    @Override
//    public String execute(String[] args, HashSet<StudyGroup> collection, Scanner scanner) {
//        try {
//            manager.save();
//            return "Коллекция сохранена в файл";
//        } catch (IOException e) {
//            return "Ошибка сохранения: " + e.getMessage();
//        }
//    }
//
//    @Override
//    public String getName() { return "save"; }
//
//    @Override
//    public String getDescription() { return "сохранить коллекцию в файл"; }
//
//    @Override
//    public String getSyntax() { return "save"; }
//}