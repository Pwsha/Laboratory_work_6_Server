package org.example.data;

import org.example.init.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Класс парсера csv файла
 * @author Pwsha
 * @version v1.3
 */
public class StudyGroupCsvParser {
    private final String filename;
    private static final String HEADER = "id,name,coordinates_x,coordinates_y,creationDate,studentsCount,expelledStudents,formOfEducation,semesterEnum,groupAdmin_name,groupAdmin_birthday,groupAdmin_weight,groupAdmin_passportID,groupAdmin_location_x,groupAdmin_location_y,groupAdmin_location_z";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public StudyGroupCsvParser(String filename) {
        this.filename = filename;
    }

    public HashSet<StudyGroup> loadFromFile() throws IOException {
        HashSet<StudyGroup> collection = new HashSet<>();
        File file = new File(filename);

        if (!file.exists()) {
            return collection;
        }

        if (!file.canRead()) {
            throw new IOException("Нет прав на чтение файла: " + filename);
        }

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            int lineNumber = 1;
            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                try {
                    StudyGroup group = parseLine(line);
                    if (group != null && group.isValid()) {
                        collection.add(group);
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка в строке " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        return collection;
    }

    public void saveToFile(HashSet<StudyGroup> collection) throws IOException {
        File file = new File(filename);
        if (file.exists() && !file.canWrite()) {
            throw new IOException("Нет прав на запись в файл: " + filename);
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
        if (parts.length < 9) {
            throw new IllegalArgumentException("Недостаточно полей. Ожидается минимум 9, получено " + parts.length);
        }

        try {
            int idx = 0;

            Long id = parseLong(parts[idx++]);

            String name = parts[idx++];
            if (name.isEmpty()) {
                throw new IllegalArgumentException("name не может быть пустым");
            }

            Float x = parseFloat(parts[idx++]);
            Long y = parseLong(parts[idx++]);
            Coordinates coordinates = new Coordinates.Builder()
                    .x(x)
                    .y(y)
                    .build();

            String dateStr = parts[idx++];
            LocalDateTime creationDate = dateStr.isEmpty() ? LocalDateTime.now() :
                    LocalDateTime.parse(dateStr, DATE_FORMATTER);

            long studentsCount = parseLong(parts[idx++]);

            int expelledStudents = parseInt(parts[idx++]);

            String formStr = parts[idx++];
            FormOfEducation formOfEducation = FormOfEducation.valueOf(formStr);

            String semesterStr = parts[idx++];
            Semester semester = Semester.valueOf(semesterStr);

            Person groupAdmin = null;
            if (parts.length > idx && !parts[idx].isEmpty()) {
                groupAdmin = parsePerson(parts, idx);
            }

            return new StudyGroup.Builder()
                    .id(id)
                    .name(name)
                    .coordinates(coordinates)
                    .creationDate(creationDate)
                    .studentsCount(studentsCount)
                    .expelledStudents(expelledStudents)
                    .formOfEducation(formOfEducation)
                    .semesterEnum(semester)
                    .groupAdmin(groupAdmin)
                    .build();

        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка парсинга: " + e.getMessage(), e);
        }
    }

    private Person parsePerson(String[] parts, int startIdx) {
        int idx = startIdx;

        try {
            String name = parts[idx++];
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Имя администратора не может быть пустым");
            }

            Date birthday = null;
            if (idx < parts.length && !parts[idx].isEmpty()) {
                long birthdayMillis = parseLong(parts[idx]);
                birthday = new Date(birthdayMillis);
            }
            idx++;

            Integer weight = null;
            if (idx < parts.length && !parts[idx].isEmpty()) {
                weight = parseInt(parts[idx]);
            }
            idx++;

            String passportID = null;
            if (idx < parts.length && !parts[idx].isEmpty()) {
                passportID = parts[idx];
            }
            idx++;

            Location location = null;
            if (parts.length > idx + 2) {
                Double locY = null;
                Float locZ = null;

                double locX = 0;
                if (!parts[idx].isEmpty()) {
                    locX = parseDouble(parts[idx]);
                }
                idx++;

                if (!parts[idx].isEmpty()) {
                    locY = parseDouble(parts[idx]);
                }
                idx++;

                if (!parts[idx].isEmpty()) {
                    locZ = parseFloat(parts[idx]);
                }

                if (locY != null && locZ != null) {
                    location = new Location.Builder()
                            .x(locX)
                            .y(locY)
                            .z(locZ)
                            .build();
                }
            }

            return new Person.Builder()
                    .name(name)
                    .birthday(birthday)
                    .weight(weight)
                    .passportID(passportID)
                    .location(location)
                    .build();

        } catch (Exception e) {
            System.err.println("Ошибка парсинга Person: " + e.getMessage());
            return null;
        }
    }

    private String formatLine(StudyGroup group) {
        StringBuilder sb = new StringBuilder();

        sb.append(group.getId()).append(",");
        sb.append(escapeCsv(group.getName())).append(",");
        sb.append(group.getCoordinates().getX()).append(",");
        sb.append(group.getCoordinates().getY()).append(",");
        sb.append(group.getCreationDate().format(DATE_FORMATTER)).append(",");
        sb.append(group.getStudentsCount()).append(",");
        sb.append(group.getExpelledStudents()).append(",");
        sb.append(group.getFormOfEducation().name()).append(",");
        sb.append(group.getSemesterEnum().name()).append(",");

        Person admin = group.getGroupAdmin();
        if (admin != null) {
            sb.append(escapeCsv(admin.getName())).append(",");

            Date birthday = admin.getBirthday();
            sb.append(birthday != null ? birthday.getTime() : "").append(",");

            sb.append(admin.getWeight() != null ? admin.getWeight() : "").append(",");
            sb.append(escapeCsv(admin.getPassportID())).append(",");

            Location loc = admin.getLocation();
            if (loc != null) {
                sb.append(loc.getX()).append(",");
                sb.append(loc.getY() != null ? loc.getY() : "").append(",");
                sb.append(loc.getZ() != null ? loc.getZ() : "");
            } else {
                sb.append(",,");
            }
        } else {
            sb.append(",,,,,,"); // 7 пустых полей для администратора
        }

        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static Long parseLong(String str) {
        if (str == null || str.trim().isEmpty()) return 0L;
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static Integer parseInt(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static Float parseFloat(String str) {
        if (str == null || str.trim().isEmpty()) return 0f;
        try {
            return Float.parseFloat(str.trim());
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public static Double parseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}