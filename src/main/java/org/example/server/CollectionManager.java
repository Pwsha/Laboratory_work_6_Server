package org.example.server;

import org.example.common.init.StudyGroup;
import org.example.server.data.StudyGroupCsvParser;

import java.io.*;
import java.util.HashSet;

public class CollectionManager {
    private final HashSet<StudyGroup> collection;
    private final StudyGroupCsvParser csvParser;

    public CollectionManager(String filename) {
        this.collection = new HashSet<>();
        this.csvParser = new StudyGroupCsvParser(filename);
        loadCollection();
    }

    public HashSet<StudyGroup> getCollection() {
        return collection;
    }

    public void add(StudyGroup group) {
        collection.add(group);
        saveCollection();
    }

    public boolean removeById(Long id) {
        boolean removed = collection.removeIf(g -> g.getId().equals(id));
        if (removed) {
            saveCollection();
        }
        return removed;
    }

    public void clear() {
        collection.clear();
        saveCollection();
    }

    public void saveCollection() {
        try {
            csvParser.saveToFile(collection);
            System.out.println("Коллекция сохранена");
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void loadCollection() {
        try {
            if (!csvParser.isFileAccessible()) {
                System.out.println("Файл не найден. Будет создана пустая коллекция.");
                csvParser.createEmptyFile();
                return;
            }

            HashSet<StudyGroup> loaded = csvParser.loadFromFile();
            collection.addAll(loaded);
            System.out.println("Загружено элементов: " + collection.size());

        } catch (IOException e) {
            System.err.println("Ошибка загрузки: " + e.getMessage());
        }
    }
}