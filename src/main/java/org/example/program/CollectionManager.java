package org.example.program;

import org.example.data.StudyGroupCsvParser;
import org.example.init.*;

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

    public void removeById(Long id) {
        collection.removeIf(g -> g.getId().equals(id));
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