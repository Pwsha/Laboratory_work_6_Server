package org.example.client;

import org.example.common.init.*;

import java.time.LocalDateTime;
import java.util.Scanner;

public class ClientHelper {
    public static StudyGroup readStudyGroup(Scanner scanner) {
        System.out.println("Введите данные группы:");

        // name
        String name = null;
        while (true) {
            System.out.print("  Название группы: ");
            name = scanner.nextLine().trim();
            if (!name.isEmpty()) break;
            System.out.println("  Ошибка: название не может быть пустым");
        }

        // coordinates
        Coordinates coordinates = readCoordinates(scanner);

        // studentsCount
        long studentsCount = 0;
        while (true) {
            System.out.print("  Количество студентов (>0): ");
            try {
                studentsCount = Long.parseLong(scanner.nextLine().trim());
                if (studentsCount > 0) break;
                System.out.println("  Ошибка: должно быть >0");
            } catch (NumberFormatException e) {
                System.out.println("  Ошибка: введите число");
            }
        }

        // expelledStudents
        int expelledStudents = 0;
        while (true) {
            System.out.print("  Количество отчисленных (>0): ");
            try {
                expelledStudents = Integer.parseInt(scanner.nextLine().trim());
                if (expelledStudents > 0) break;
                System.out.println("  Ошибка: должно быть >0");
            } catch (NumberFormatException e) {
                System.out.println("  Ошибка: введите число");
            }
        }

        // formOfEducation
        FormOfEducation formOfEducation = readEnum(scanner, FormOfEducation.class, "форму обучения");

        // semesterEnum
        Semester semester = readEnum(scanner, Semester.class, "семестр");

        // groupAdmin
        Person admin = null;
        System.out.print("  Добавить администратора? (y/n): ");
        if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
            admin = readPerson(scanner);
        }

        // Создаём объект через Builder
        return new StudyGroup.Builder()
                .name(name)
                .coordinates(coordinates)
                .creationDate(LocalDateTime.now())
                .studentsCount(studentsCount)
                .expelledStudents(expelledStudents)
                .formOfEducation(formOfEducation)
                .semesterEnum(semester)
                .groupAdmin(admin)
                .build();
    }

    private static Coordinates readCoordinates(Scanner scanner) {
        Float x = null;
        while (true) {
            System.out.print("  Координата x (<=741): ");
            try {
                x = Float.parseFloat(scanner.nextLine().trim());
                if (x <= 741) break;
                System.out.println("  Ошибка: x <= 741");
            } catch (NumberFormatException e) {
                System.out.println("  Ошибка: введите число");
            }
        }

        long y = 0;
        while (true) {
            System.out.print("  Координата y (>-938): ");
            try {
                y = Long.parseLong(scanner.nextLine().trim());
                if (y > -938) break;
                System.out.println("  Ошибка: y > -938");
            } catch (NumberFormatException e) {
                System.out.println("  Ошибка: введите число");
            }
        }

        return new Coordinates.Builder()
                .x(x)
                .y(y)
                .build();
    }

    private static Person readPerson(Scanner scanner) {
        System.out.println("  Данные администратора:");

        // name
        String name = null;
        while (true) {
            System.out.print("    Имя: ");
            name = scanner.nextLine().trim();
            if (!name.isEmpty()) break;
            System.out.println("    Ошибка: имя не может быть пустым");
        }

        // birthday
        java.util.Date birthday = null;
        System.out.print("    Дата рождения (yyyy-MM-dd) или пусто: ");
        String dateStr = scanner.nextLine().trim();
        if (!dateStr.isEmpty()) {
            try {
                birthday = java.sql.Date.valueOf(dateStr);
            } catch (IllegalArgumentException e) {
                System.out.println("    Неверный формат, поле не будет установлено");
            }
        }

        // weight
        Integer weight = null;
        while (true) {
            System.out.print("    Вес (>0): ");
            try {
                weight = Integer.parseInt(scanner.nextLine().trim());
                if (weight > 0) break;
                System.out.println("    Ошибка: вес >0");
            } catch (NumberFormatException e) {
                System.out.println("    Ошибка: введите число");
            }
        }

        // passportID
        String passportID = null;
        while (true) {
            System.out.print("    Номер паспорта (<=20 символов): ");
            passportID = scanner.nextLine().trim();
            if (!passportID.isEmpty() && passportID.length() <= 20) break;
            System.out.println("    Ошибка: от 1 до 20 символов");
        }

        // location
        Location location = null;
        System.out.print("    Добавить местоположение? (y/n): ");
        if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
            location = readLocation(scanner);
        }

        return new Person.Builder()
                .name(name)
                .birthday(birthday)
                .weight(weight)
                .passportID(passportID)
                .location(location)
                .build();
    }

    private static Location readLocation(Scanner scanner) {
        System.out.println("    Местоположение:");

        // x
        double x = 0;
        System.out.print("      x: ");
        try {
            x = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("      Неверный формат, установлено 0");
        }

        // y
        Double y = null;
        while (true) {
            System.out.print("      y (не null): ");
            try {
                y = Double.parseDouble(scanner.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("      Ошибка: введите число");
            }
        }

        // z
        Float z = null;
        while (true) {
            System.out.print("      z (не null): ");
            try {
                z = Float.parseFloat(scanner.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("      Ошибка: введите число");
            }
        }

        return new Location.Builder()
                .x(x)
                .y(y)
                .z(z)
                .build();
    }

    private static <T extends Enum<T>> T readEnum(Scanner scanner, Class<T> enumClass, String description) {
        T[] constants = enumClass.getEnumConstants();
        System.out.println("  Доступные варианты " + description + ":");
        for (T c : constants) {
            System.out.println("    " + c.name());
        }

        while (true) {
            System.out.print("  Выберите " + description + ": ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return Enum.valueOf(enumClass, input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Ошибка: неверное значение");
            }
        }
    }
}