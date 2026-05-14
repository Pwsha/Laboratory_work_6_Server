package org.example.server;

import org.example.common.init.StudyGroup;
import org.example.server.data.DataManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CollectionManager {
    private final Set<StudyGroup> collection = Collections.synchronizedSet(new HashSet<>());
    private final DataManager dbManager;

    public CollectionManager(DataManager dbManager) {
        this.dbManager = dbManager;
        refreshFromDatabase();
    }

    public void refreshFromDatabase() {
        synchronized (collection) {
            collection.clear();
            collection.addAll(dbManager.loadAllGroups());
        }
        System.out.println("Коллекция обновлена из БД. Размер: " + collection.size());
    }

    public Set<StudyGroup> getCollection() {
        return collection;
    }

    public boolean add(StudyGroup group, int userId) {
        Long generatedId = dbManager.addGroup(group, userId);
        if (generatedId != null) {
            group.setId(generatedId);
            synchronized (collection) {
                collection.add(group);
            }
            return true;
        }
        return false;
    }

    public boolean update(Long id, StudyGroup newGroup, int userId) {
        if (dbManager.updateGroup(id, newGroup, userId)) {
            synchronized (collection) {
                collection.removeIf(g -> g.getId().equals(id));
                collection.add(newGroup);
            }
            return true;
        }
        return false;
    }

    public boolean removeById(Long id, int userId) {
        if (dbManager.deleteGroup(id, userId)) {
            synchronized (collection) {
                collection.removeIf(g -> g.getId().equals(id));
            }
            return true;
        }
        return false;
    }

    public boolean clear(int userId) {
        if (dbManager.clearGroups(userId)) {
            synchronized (collection) {
                collection.removeIf(g -> {
                    return true;
                });
            }
            refreshFromDatabase();
            return true;
        }
        return false;
    }
}