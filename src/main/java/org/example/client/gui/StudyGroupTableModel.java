package org.example.client.gui;

import javafx.beans.property.*;
import org.example.common.init.StudyGroup;

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

    private StudyGroupTableModel(Builder builder) {
        this.id.set(builder.id);
        this.name.set(builder.name);
        this.x.set(builder.x);
        this.y.set(builder.y);
        this.creationDate.set(builder.creationDate);
        this.studentsCount.set(builder.studentsCount);
        this.expelledStudents.set(builder.expelledStudents);
        this.formOfEducation.set(builder.formOfEducation);
        this.semesterEnum.set(builder.semesterEnum);
        this.userId.set(builder.userId);
        this.currentUserId.set(builder.currentUserId);
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
        return result;
    }

    public static StudyGroupTableModel from(StudyGroup group, int currentUserId) {
        Integer groupUserId = group.getUserId();
        if (groupUserId == null) {
            groupUserId = 0;
        }

        return new Builder()
                .id(group.getId() != null ? group.getId() : 0)
                .name(group.getName() != null ? group.getName() : "")
                .x(group.getCoordinates() != null ? group.getCoordinates().getX() : 0f)
                .y(group.getCoordinates() != null ? group.getCoordinates().getY() : 0L)
                .creationDate(group.getCreationDate() != null ? group.getCreationDate() : LocalDateTime.now())
                .studentsCount(group.getStudentsCount())
                .expelledStudents(group.getExpelledStudents())
                .formOfEducation(group.getFormOfEducation() != null ? group.getFormOfEducation().name() : "")
                .semesterEnum(group.getSemesterEnum() != null ? group.getSemesterEnum().name() : "")
                .userId(groupUserId)
                .currentUserId(currentUserId)
                .build();
    }

    public static class Builder {
        private long id;
        private String name;
        private float x;
        private long y;
        private LocalDateTime creationDate;
        private long studentsCount;
        private int expelledStudents;
        private String formOfEducation;
        private String semesterEnum;
        private int userId;
        private int currentUserId;

        public Builder id(long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder x(float x) { this.x = x; return this; }
        public Builder y(long y) { this.y = y; return this; }
        public Builder creationDate(LocalDateTime creationDate) { this.creationDate = creationDate; return this; }
        public Builder studentsCount(long studentsCount) { this.studentsCount = studentsCount; return this; }
        public Builder expelledStudents(int expelledStudents) { this.expelledStudents = expelledStudents; return this; }
        public Builder formOfEducation(String formOfEducation) { this.formOfEducation = formOfEducation; return this; }
        public Builder semesterEnum(String semesterEnum) { this.semesterEnum = semesterEnum; return this; }
        public Builder userId(int userId) { this.userId = userId; return this; }
        public Builder currentUserId(int currentUserId) { this.currentUserId = currentUserId; return this; }

        public StudyGroupTableModel build() {
            return new StudyGroupTableModel(this);
        }
    }
}