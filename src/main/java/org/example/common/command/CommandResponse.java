package org.example.common.command;

import org.example.common.init.StudyGroup;
import java.io.Serializable;
import java.util.List;

public class CommandResponse implements Serializable {
    private static final long serialVersionUID = 2L;

    private final boolean success;
    private final String message;
    private final List<StudyGroup> collection;
    private final StudyGroup group;
    private final Long count;
    private final Integer userId;

    private CommandResponse(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.collection = builder.collection;
        this.group = builder.group;
        this.count = builder.count;
        this.userId = builder.userId;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<StudyGroup> getCollection() { return collection; }
    public StudyGroup getGroup() { return group; }
    public Long getCount() { return count; }
    public Integer getUserId() { return userId; }

    public static Builder builder() {
        return new Builder();
    }

    public static CommandResponse success(String message) {
        return builder().success(true).message(message).build();
    }

    public static CommandResponse error(String message) {
        return builder().success(false).message(message).build();
    }

    public static CommandResponse withCollection(String message, List<StudyGroup> collection) {
        return builder().success(true).message(message).collection(collection).build();
    }

    public static CommandResponse withGroup(String message, StudyGroup group) {
        return builder().success(true).message(message).group(group).build();
    }

    public static CommandResponse withCount(String message, Long count) {
        return builder().success(true).message(message).count(count).build();
    }

    public static class Builder {
        private boolean success;
        private String message;
        private List<StudyGroup> collection;
        private StudyGroup group;
        private Long count;
        private Integer userId;

        public Builder success(boolean success) { this.success = success; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder collection(List<StudyGroup> collection) { this.collection = collection; return this; }
        public Builder group(StudyGroup group) { this.group = group; return this; }
        public Builder count(Long count) { this.count = count; return this; }
        public Builder userId(Integer userId) { this.userId = userId; return this; }

        public CommandResponse build() { return new CommandResponse(this); }
    }
}