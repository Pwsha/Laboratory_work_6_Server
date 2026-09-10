package org.example.server;

import org.example.common.init.StudyGroup;
import org.example.server.data.DataManager;

import java.util.*;

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
            group.setUserId(userId);
            synchronized (collection) {
                collection.add(group);
            }
            return true;
        }
        return false;
    }

    public boolean update(Long id, StudyGroup newGroup, int userId) {
        newGroup.setUserId(userId);
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
        StudyGroup group = null;
        synchronized (collection) {
            group = collection.stream()
                    .filter(g -> g.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        if (group == null) {
            return false;
        }

        if (group.getUserId() != null && group.getUserId() != userId) {
            return false;
        }

        if (dbManager.deleteGroup(id, userId)) {
            synchronized (collection) {
                collection.removeIf(g -> g.getId().equals(id));
            }
            return true;
        }
        return false;
    }

    public boolean clear(int userId) {
        List<Long> idsToRemove = new ArrayList<>();
        synchronized (collection) {
            for (StudyGroup g : collection) {
                if (g.getUserId() != null && g.getUserId() == userId) {
                    idsToRemove.add(g.getId());
                }
            }
        }
        boolean dbSuccess = dbManager.clearGroups(userId);

        if (dbSuccess) {
            synchronized (collection) {
                int before = collection.size();
                collection.removeIf(g -> g.getUserId() != null && g.getUserId() == userId);
                int after = collection.size();
            }
            return true;
        }
        return false;
    }
}