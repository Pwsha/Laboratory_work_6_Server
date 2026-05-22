package org.example.client.gui;

import javafx.beans.property.*;
import org.example.common.init.*;
import java.time.LocalDateTime;

public class StudyGroupTableModel {
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final FloatProperty x = new SimpleFloatProperty();
    private final LongProperty y = new SimpleLongProperty();
    private final ObjectProperty<LocalDateTime> creationDate = new SimpleObjectProperty<>();
    private final LongProperty studentsCount = new SimpleLongProperty();
    private final IntegerProperty expelledStudents = new SimpleIntegerProperty();
    private final StringProperty formOfEducation = new SimpleStringProperty();
    private final StringProperty semesterEnum = new SimpleStringProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final IntegerProperty currentUserId = new SimpleIntegerProperty();

    public StudyGroupTableModel(StudyGroup group, int currentUserId) {
        this.id.set(group.getId() != null ? group.getId() : 0);
        this.name.set(group.getName() != null ? group.getName() : "");
        this.x.set(group.getCoordinates() != null ? group.getCoordinates().getX() : 0f);
        this.y.set(group.getCoordinates() != null ? group.getCoordinates().getY() : 0L);
        this.creationDate.set(group.getCreationDate() != null ? group.getCreationDate() : LocalDateTime.now());
        this.studentsCount.set(group.getStudentsCount());
        this.expelledStudents.set(group.getExpelledStudents());
        this.formOfEducation.set(group.getFormOfEducation() != null ? group.getFormOfEducation().name() : "");
        this.semesterEnum.set(group.getSemesterEnum() != null ? group.getSemesterEnum().name() : "");
        this.userId.set(group.getUserId() != null ? group.getUserId() : 0);
        this.currentUserId.set(currentUserId);
    }

    public long getId() { return id.get(); }
    public LongProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public float getX() { return x.get(); }
    public FloatProperty xProperty() { return x; }

    public long getY() { return y.get(); }
    public LongProperty yProperty() { return y; }

    public LocalDateTime getCreationDate() { return creationDate.get(); }
    public ObjectProperty<LocalDateTime> creationDateProperty() { return creationDate; }

    public long getStudentsCount() { return studentsCount.get(); }
    public LongProperty studentsCountProperty() { return studentsCount; }

    public int getExpelledStudents() { return expelledStudents.get(); }
    public IntegerProperty expelledStudentsProperty() { return expelledStudents; }

    public String getFormOfEducation() { return formOfEducation.get(); }
    public StringProperty formOfEducationProperty() { return formOfEducation; }

    public String getSemesterEnum() { return semesterEnum.get(); }
    public StringProperty semesterEnumProperty() { return semesterEnum; }

    public int getUserId() { return userId.get(); }
    public IntegerProperty userIdProperty() { return userId; }

    public boolean isOwnedByCurrentUser() {
        boolean result = userId.get() == currentUserId.get();
        System.out.println("DEBUG isOwned: userId=" + userId.get() + ", currentUserId=" + currentUserId.get() + ", result=" + result);
        return result;
    }

    public static StudyGroupTableModel from(StudyGroup group, int currentUserId) {
        return new StudyGroupTableModel(group, currentUserId);
    }
}