package org.example.common.command;

import org.example.common.init.StudyGroup;
import java.io.Serializable;

public class CommandRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final CommandType type;
    private final String stringArg;
    private final StudyGroup studyGroup;
    private final Long id;
    private final Long studentsCount;
    private final Integer expelledStudents;

    private CommandRequest(Builder builder) {
        this.type = builder.type;
        this.stringArg = builder.stringArg;
        this.studyGroup = builder.studyGroup;
        this.id = builder.id;
        this.studentsCount = builder.studentsCount;
        this.expelledStudents = builder.expelledStudents;
    }

    // Геттеры
    public CommandType getType() { return type; }
    public String getStringArg() { return stringArg; }
    public StudyGroup getStudyGroup() { return studyGroup; }
    public Long getId() { return id; }
    public Long getStudentsCount() { return studentsCount; }
    public Integer getExpelledStudents() { return expelledStudents; }

    public static class Builder {
        private CommandType type;
        private String stringArg;
        private StudyGroup studyGroup;
        private Long id;
        private Long studentsCount;
        private Integer expelledStudents;

        public Builder type(CommandType type) {
            this.type = type;
            return this;
        }

        public Builder stringArg(String stringArg) { this.stringArg = stringArg; return this; }

        public Builder studyGroup(StudyGroup studyGroup) {
            this.studyGroup = studyGroup;
            return this;
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder studentsCount(Long studentsCount) {
            this.studentsCount = studentsCount;
            return this;
        }

        public Builder expelledStudents(Integer expelledStudents) {
            this.expelledStudents = expelledStudents;
            return this;
        }

        public CommandRequest build() {
            return new CommandRequest(this);
        }
    }

    @Override
    public String toString() {
        return "CommandRequest{type=" + type + "}";
    }
}