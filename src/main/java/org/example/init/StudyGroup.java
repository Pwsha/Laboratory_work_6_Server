package org.example.init;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Класс для инициализации студенческой группы
 * @author Pwsha
 * @version v1.3
 */
public class StudyGroup implements Comparable<StudyGroup> {
    private final Long id; // Поле не может быть null, >0, уникальное, генерируется автоматически
    private final String name; // Поле не может быть null, не пустое
    private final Coordinates coordinates; // Поле не может быть null
    private final LocalDateTime creationDate; // Поле не может быть null, генерируется автоматически
    private final long studentsCount; // >0
    private final int expelledStudents; // >0
    private final FormOfEducation formOfEducation; // Поле не может быть null
    private final Semester semesterEnum; // Поле не может быть null
    private final Person groupAdmin; // Может быть null

    /**
     * Конструктор со всеми значениями
     * @param builder
     */
    private StudyGroup(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.coordinates = builder.coordinates;
        this.creationDate = builder.creationDate;
        this.studentsCount = builder.studentsCount;
        this.expelledStudents = builder.expelledStudents;
        this.formOfEducation = builder.formOfEducation;
        this.semesterEnum = builder.semesterEnum;
        this.groupAdmin = builder.groupAdmin;
    }

    /**
     * Класс Билдер для построения конструкторов
     */
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

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder coordinates(Coordinates coordinates) {
            this.coordinates = coordinates;
            return this;
        }

        public Builder creationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Builder studentsCount(long studentsCount) {
            this.studentsCount = studentsCount;
            return this;
        }

        public Builder expelledStudents(int expelledStudents) {
            this.expelledStudents = expelledStudents;
            return this;
        }

        public Builder formOfEducation(FormOfEducation formOfEducation) {
            this.formOfEducation = formOfEducation;
            return this;
        }

        public Builder semesterEnum(Semester semesterEnum) {
            this.semesterEnum = semesterEnum;
            return this;
        }

        public Builder groupAdmin(Person groupAdmin) {
            this.groupAdmin = groupAdmin;
            return this;
        }

        public StudyGroup build() {
            // Валидация
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("id должен быть > 0");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("name не может быть пустым");
            }
            if (coordinates == null) {
                throw new IllegalArgumentException("coordinates не может быть null");
            }
            if (creationDate == null) {
                this.creationDate = LocalDateTime.now();
            }
            if (studentsCount <= 0) {
                throw new IllegalArgumentException("studentsCount должен быть > 0");
            }
            if (expelledStudents <= 0) {
                throw new IllegalArgumentException("expelledStudents должен быть > 0");
            }
            if (formOfEducation == null) {
                throw new IllegalArgumentException("formOfEducation не может быть null");
            }
            if (semesterEnum == null) {
                throw new IllegalArgumentException("semesterEnum не может быть null");
            }

            return new StudyGroup(this);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public long getStudentsCount() {
        return studentsCount;
    }

    public int getExpelledStudents() {
        return expelledStudents;
    }

    public FormOfEducation getFormOfEducation() {
        return formOfEducation;
    }

    public Semester getSemesterEnum() {
        return semesterEnum;
    }

    public Person getGroupAdmin() {
        return groupAdmin;
    }

    public boolean isValid() {
        try {
            return id != null && id > 0 &&
                    name != null && !name.trim().isEmpty() &&
                    coordinates != null &&
                    creationDate != null &&
                    studentsCount > 0 &&
                    expelledStudents > 0 &&
                    formOfEducation != null &&
                    semesterEnum != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Метод сортировки
     * @param other
     * @return compareTo(id)
     */
    @Override
    public int compareTo(StudyGroup other) {

        int studentsCompare = Long.compare(this.studentsCount, other.studentsCount);
        if (studentsCompare != 0) return studentsCompare;

        int expelledCompare = Integer.compare(this.expelledStudents, other.expelledStudents);
        if (expelledCompare != 0) return expelledCompare;

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
        return String.format("StudyGroup[id=%d, name='%s', x=%.1f, y=%d, data=%tT %tF, students=%d, form=%s, semester=%s]",
                id, name, coordinates.getX(), coordinates.getY(), creationDate, creationDate, studentsCount, formOfEducation, semesterEnum);
    }
}