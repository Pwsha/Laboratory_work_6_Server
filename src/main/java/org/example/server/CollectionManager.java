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
        collection.add(group);
    }

    public void clear() {
        collection.clear();
    }

    public boolean removeById(Long id) {
        collection.removeIf(g -> g.getId().equals(id));
        return false;
    }

    public void save() throws IOException {
        csvParser.saveToFile(collection);
    }

    private void loadCollection() {
        try {
            if (!csvParser.isFileAccessible()) {
                csvParser.createEmptyFile();
                return;
            }
            collection.addAll(csvParser.loadFromFile());
            System.out.println("Загружено элементов: " + collection.size());
        } catch (IOException e) {
            System.out.println("Ошибка загрузки: " + e.getMessage());
        }
    }

}