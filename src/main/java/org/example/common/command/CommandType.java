package org.example.common.command;

import java.io.Serializable;

public enum CommandType implements Serializable {
    HELP,
    INFO,
    SHOW,
    SHOW_ODD,
    ADD,
    EXIT,
    UPDATE,
    REMOVE_BY_ID,
    CLEAR,
    ADD_IF_MAX,
    REMOVE_GREATER,
    HISTORY,
    REMOVE_ANY_BY_STUDENTS_COUNT,
    MIN_BY_SEMESTER_ENUM,
    COUNT_GREATER_THAN_EXPELLED_STUDENTS,
    EXECUTE_SCRIPT,
    LOGIN,
    REGISTER,
    LOGOUT,
    GET_USER_ID;

    public static CommandType fromString(String name) {
        try {
            return CommandType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}