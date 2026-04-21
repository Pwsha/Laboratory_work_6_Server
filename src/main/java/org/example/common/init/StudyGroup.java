package org.example.common.init;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class StudyGroup implements Comparable<StudyGroup>, Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Coordinates coordinates;
    private LocalDateTime creationDate;
    private long studentsCount;
    private int expelledStudents;
    private FormOfEducation formOfEducation;
    private Semester semesterEnum;
    private Person groupAdmin;

    public StudyGroup() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Coordinates getCoordinates() { return coordinates; }
    public void setCoordinates(Coordinates coordinates) { this.coordinates = coordinates; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public long getStudentsCount() { return studentsCount; }
    public void setStudentsCount(long studentsCount) { this.studentsCount = studentsCount; }

    public int getExpelledStudents() { return expelledStudents; }
    public void setExpelledStudents(int expelledStudents) { this.expelledStudents = expelledStudents; }

    public FormOfEducation getFormOfEducation() { return formOfEducation; }
    public void setFormOfEducation(FormOfEducation formOfEducation) { this.formOfEducation = formOfEducation; }

    public Semester getSemesterEnum() { return semesterEnum; }
    public void setSemesterEnum(Semester semesterEnum) { this.semesterEnum = semesterEnum; }

    public Person getGroupAdmin() { return groupAdmin; }
    public void setGroupAdmin(Person groupAdmin) { this.groupAdmin = groupAdmin; }

    @Override
    public int compareTo(StudyGroup other) {
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudyGroup that = (StudyGroup) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("StudyGroup[id=%d, name='%s', students=%d, form=%s, semester=%s]",
                id, name, studentsCount, formOfEducation, semesterEnum);
    }

    public static class Builder {
        private Long id;
        private String name;
        private Coordinates coordinates;
        private LocalDateTime creationDate;
        private long studentsCount;
        private int expelledStudents;
        private FormOfEducation formOfEducation;
        private Semester semesterEnum;
        private Person groupAdmin;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder coordinates(Coordinates coordinates) { this.coordinates = coordinates; return this; }
        public Builder creationDate(LocalDateTime creationDate) { this.creationDate = creationDate; return this; }
        public Builder studentsCount(long studentsCount) { this.studentsCount = studentsCount; return this; }
        public Builder expelledStudents(int expelledStudents) { this.expelledStudents = expelledStudents; return this; }
        public Builder formOfEducation(FormOfEducation formOfEducation) { this.formOfEducation = formOfEducation; return this; }
        public Builder semesterEnum(Semester semesterEnum) { this.semesterEnum = semesterEnum; return this; }
        public Builder groupAdmin(Person groupAdmin) { this.groupAdmin = groupAdmin; return this; }

        public StudyGroup build() {
            StudyGroup group = new StudyGroup();
            group.setId(id);
            group.setName(name);
            group.setCoordinates(coordinates);
            group.setCreationDate(creationDate);
            group.setStudentsCount(studentsCount);
            group.setExpelledStudents(expelledStudents);
            group.setFormOfEducation(formOfEducation);
            group.setSemesterEnum(semesterEnum);
            group.setGroupAdmin(groupAdmin);
            return group;
        }
    }
}