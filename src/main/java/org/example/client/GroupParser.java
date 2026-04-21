package org.example.client;

import org.example.common.init.StudyGroup;
import org.example.common.init.Coordinates;
import org.example.common.init.FormOfEducation;
import org.example.common.init.Semester;

import java.util.HashSet;

public class GroupParser {

    public static StudyGroup parseFromString(String input, HashSet<StudyGroup> collection) {
        if (collection == null) {
            collection = new HashSet<>();
        }

        String[] parts = input.split(",");

        if (parts.length < 7) {
            throw new IllegalArgumentException("Недостаточно данных. Минимум 7 полей: name,x,y,studentsCount,expelledStudents,formOfEducation,semesterEnum");
        }

        try {
            int idx = 0;

            String name = parts[idx++].trim();
            Float x = Float.parseFloat(parts[idx++].trim());
            Long y = Long.parseLong(parts[idx++].trim());
            long studentsCount = Long.parseLong(parts[idx++].trim());
            int expelledStudents = Integer.parseInt(parts[idx++].trim());
            FormOfEducation form = FormOfEducation.valueOf(parts[idx++].trim().toUpperCase());
            Semester semester = Semester.valueOf(parts[idx++].trim().toUpperCase());

            Coordinates coordinates = new Coordinates.Builder().x(x).y(y).build();

            StudyGroup group = new StudyGroup();
            group.setName(name);
            group.setCoordinates(coordinates);
            group.setStudentsCount(studentsCount);
            group.setExpelledStudents(expelledStudents);
            group.setFormOfEducation(form);
            group.setSemesterEnum(semester);

            return group;
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка парсинга: " + e.getMessage());
        }
    }
}