package org.example.common.command;

import org.example.common.init.StudyGroup;
import java.io.Serializable;

public class CommandRequest implements Serializable {
    private static final long serialVersionUID = 2L;

    private final CommandType type;
    private final String stringArg;
    private final StudyGroup studyGroup;
    private final Long id;
    private final Long studentsCount;
    private final Integer expelledStudents;
    private final String login;
    private final String password;

    private CommandRequest(Builder builder) {
        this.type = builder.type;
        this.stringArg = builder.stringArg;
        this.studyGroup = builder.studyGroup;
        this.id = builder.id;
        this.studentsCount = builder.studentsCount;
        this.expelledStudents = builder.expelledStudents;
        this.login = builder.login;
        this.password = builder.password;
    }

    public CommandType getType() { return type; }
    public String getStringArg() { return stringArg; }
    public StudyGroup getStudyGroup() { return studyGroup; }
    public Long getId() { return id; }
    public Long getStudentsCount() { return studentsCount; }
    public Integer getExpelledStudents() { return expelledStudents; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }

    public static class Builder {
        private CommandType type;
        private String stringArg;
        private StudyGroup studyGroup;
        private Long id;
        private Long studentsCount;
        private Integer expelledStudents;
        private String login;
        private String password;

        public Builder type(CommandType type) { this.type = type; return this; }
        public Builder stringArg(String stringArg) { this.stringArg = stringArg; return this; }
        public Builder studyGroup(StudyGroup studyGroup) { this.studyGroup = studyGroup; return this; }
        public Builder id(Long id) { this.id = id; return this; }
        public Builder studentsCount(Long studentsCount) { this.studentsCount = studentsCount; return this; }
        public Builder expelledStudents(Integer expelledStudents) { this.expelledStudents = expelledStudents; return this; }
        public Builder login(String login) { this.login = login; return this; }
        public Builder password(String password) { this.password = password; return this;}

        public CommandRequest build() { return new CommandRequest(this); }
    }
}