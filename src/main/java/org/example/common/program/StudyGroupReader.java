package org.example.common.program;

import org.example.server.CommandHelper;
import org.example.server.GroupParser;
import org.example.common.init.StudyGroup;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Класс для чтения StudyGroup с установкой режима ввода
 * @author Pwsha
 * @version v1.3
 */
public class StudyGroupReader {
    private static boolean isScriptMode = false;
    private static String scriptInput = null;

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
        if (isScriptMode && scriptInput != null) {
            return GroupParser.parseFromString(scriptInput, collection);
        } else {
            return CommandHelper.readStudyGroup(scanner, collection);
        }
    }
}