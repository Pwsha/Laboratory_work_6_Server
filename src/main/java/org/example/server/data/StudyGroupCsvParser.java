package org.example.server.data;

import org.example.common.init.StudyGroup;
import org.example.common.init.Coordinates;
import org.example.common.init.FormOfEducation;
import org.example.common.init.Semester;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StudyGroupCsvParser {
    private final String filename;
    private static final String HEADER = "id,name,coordinates_x,coordinates_y,creationDate,studentsCount,expelledStudents,formOfEducation,semesterEnum,groupAdmin_name,groupAdmin_birthday,groupAdmin_weight,groupAdmin_passportID,groupAdmin_location_x,groupAdmin_location_y,groupAdmin_location_z";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public StudyGroupCsvParser(String filename) {
        this.filename = filename;
    }

    public HashSet<StudyGroup> loadFromFile() throws IOException {
        HashSet<StudyGroup> collection = new HashSet<>();
        File file = new File(filename);

        if (!file.exists()) {
            return collection;
        }

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                StudyGroup group = parseLine(line);
                if (group != null) {
                    collection.add(group);
                }
            }
        }

        return collection;
    }

    public void saveToFile(HashSet<StudyGroup> collection) throws IOException {
        File file = new File(filename);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(HEADER);
            for (StudyGroup group : collection) {
                writer.println(formatLine(group));
            }
        }
    }

    public boolean isFileAccessible() {
        File file = new File(filename);
        return file.exists() && file.canRead();
    }

    public void createEmptyFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(HEADER);
        }
    }

    private StudyGroup parseLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 9) return null;

        try {
            int idx = 0;

            Long id = Long.parseLong(parts[idx++]);
            String name = parts[idx++];

            Float x = Float.parseFloat(parts[idx++]);
            Long y = Long.parseLong(parts[idx++]);
            Coordinates coordinates = new Coordinates.Builder()
                    .x(x)
                    .y(y)
                    .build();

            String dateStr = parts[idx++];
            LocalDateTime creationDate = dateStr.isEmpty() ? LocalDateTime.now() :
                    LocalDateTime.parse(dateStr, DATE_FORMATTER);

            long studentsCount = Long.parseLong(parts[idx++]);
            int expelledStudents = Integer.parseInt(parts[idx++]);
            FormOfEducation formOfEducation = FormOfEducation.valueOf(parts[idx++]);
            Semester semesterEnum = Semester.valueOf(parts[idx++]);

            return new StudyGroup.Builder()
                    .id(id)
                    .name(name)
                    .coordinates(coordinates)
                    .creationDate(creationDate)
                    .studentsCount(studentsCount)
                    .expelledStudents(expelledStudents)
                    .formOfEducation(formOfEducation)
                    .semesterEnum(semesterEnum)
                    .build();

        } catch (Exception e) {
            return null;
        }
    }

    private String formatLine(StudyGroup group) {
        return String.format("%d,%s,%f,%d,%s,%d,%d,%s,%s,,,,,,,",
                group.getId(),
                escapeCsv(group.getName()),
                group.getCoordinates().getX(),
                group.getCoordinates().getY(),
                group.getCreationDate().format(DATE_FORMATTER),  // теперь без наносекунд
                group.getStudentsCount(),
                group.getExpelledStudents(),
                group.getFormOfEducation().name(),
                group.getSemesterEnum().name()
        );
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}