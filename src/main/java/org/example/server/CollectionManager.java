package org.example.server;

import org.example.server.data.StudyGroupCsvParser;
import org.example.common.init.StudyGroup;

import java.io.IOException;
import java.util.HashSet;

/**
 * Класс управления коллекцией
 * @author Pwsha
 * @version v1.3
 */
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
        CommandHelper.prepareForSave(group, getCollection());
        collection.add(group);
        saveCollection();
    }

    public void clear() {
        collection.clear();
    }

    public boolean removeById(Long id) {
        collection.removeIf(g -> g.getId().equals(id));
        return false;
    }

    public void saveCollection() {
        csvParser.saveToFile(collection);
        System.out.println("Коллекция сохранена");
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