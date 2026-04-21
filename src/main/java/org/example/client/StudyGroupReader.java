package org.example.client;

import org.example.common.init.StudyGroup;

import java.util.HashSet;
import java.util.Scanner;

public class StudyGroupReader {
    private static boolean isScriptMode = false;
    private static String scriptInput = null;
    private static final HashSet<StudyGroup> EMPTY_COLLECTION = new HashSet<>();

    public static void setScriptMode(String input) {
        isScriptMode = true;
        scriptInput = input;
        if (scriptInput.startsWith("{") && scriptInput.endsWith("}")) {
            scriptInput = scriptInput.substring(1, scriptInput.length() - 1);
        }
    }

    public static void setConsoleMode() {
        isScriptMode = false;
        scriptInput = null;
    }

    public static void reset() {
        isScriptMode = false;
        scriptInput = null;
    }

    public static StudyGroup read(HashSet<StudyGroup> collection, Scanner scanner) {
        if (collection == null) {
            collection = EMPTY_COLLECTION;
        }

        if (isScriptMode && scriptInput != null) {
            return GroupParser.parseFromString(scriptInput, collection);
        } else {
            return CommandHelper.readStudyGroup(scanner, collection);
        }
    }

    public static boolean isScriptMode() {
        return isScriptMode;
    }
}