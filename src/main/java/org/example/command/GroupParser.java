package org.example.command;

import org.example.init.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Date;

/**
 * Класс для прочтения группы из файла
 * @author Pwsha
 * @version v1.3
 */
public class GroupParser {
    public static StudyGroup parseFromString(String input, HashSet<StudyGroup> collection) {
        String[] parts = input.split(",");

        if (parts.length < 7) {
            throw new IllegalArgumentException("Недостаточно данных. Минимум 7 полей: name,x,y,studentsCount,expelledStudents,formOfEducation,semesterEnum");
        }

        try {
            int idx = 0;

            String name = parts[idx++].trim();
            if (name.isEmpty()) throw new IllegalArgumentException("name не может быть пустым");

            Float x = Float.parseFloat(parts[idx++]);
            Long y = Long.parseLong(parts[idx++]);

            Coordinates coordinates = new Coordinates.Builder()
                    .x(x)
                    .y(y)
                    .build();

            long studentsCount = Long.parseLong(parts[idx++]);
            int expelledStudents = Integer.parseInt(parts[idx++]);

            FormOfEducation formOfEducation = FormOfEducation.valueOf(parts[idx++].toUpperCase());
            Semester semester = Semester.valueOf(parts[idx++].toUpperCase());

            Person groupAdmin = null;
            if (parts.length > idx) {
                groupAdmin = parsePerson(parts, idx);
            }

            return new StudyGroup.Builder()
                    .id(CommandHelper.generateId(collection))
                    .name(name)
                    .coordinates(coordinates)
                    .creationDate(LocalDateTime.now())
                    .studentsCount(studentsCount)
                    .expelledStudents(expelledStudents)
                    .formOfEducation(formOfEducation)
                    .semesterEnum(semester)
                    .groupAdmin(groupAdmin)
                    .build();

        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка обработки: " + e.getMessage());
        }
    }

    private static Person parsePerson(String[] parts, int startIdx) {
        if (parts.length <= startIdx) return null;

        try {
            int idx = startIdx;

            String name = parts[idx++].trim();
            if (name.isEmpty()) return null;

            Date birthday = null;
            if (idx < parts.length && !parts[idx].isEmpty()) {
                try {
                    birthday = java.sql.Date.valueOf(parts[idx]);
                } catch (Exception e) {
                    try {
                        birthday = new Date(Long.parseLong(parts[idx]));
                    } catch (Exception ex) {

                    }
                }
            }
            idx++;

            Integer weight = null;
            if (idx < parts.length && !parts[idx].isEmpty()) {
                weight = Integer.parseInt(parts[idx]);
            }
            idx++;

            String passportID = null;
            if (idx < parts.length && !parts[idx].isEmpty()) {
                passportID = parts[idx].trim();
            }
            idx++;

            Location location = null;
            if (parts.length > idx + 2) {
                double locX = 0;
                Double locY = null;
                Float locZ = null;

                if (!parts[idx].isEmpty()) {
                    locX = Double.parseDouble(parts[idx]);
                }
                idx++;

                if (!parts[idx].isEmpty()) {
                    locY = Double.parseDouble(parts[idx]);
                }
                idx++;

                if (!parts[idx].isEmpty()) {
                    locZ = Float.parseFloat(parts[idx]);
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
            return null;
        }
    }
}