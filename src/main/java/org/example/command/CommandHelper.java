package org.example.command;

import org.example.init.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Вспомогательный класс для прочтения данных с клавиатуры
 * @author Pwsha
 * @version v1.3
 */
public class CommandHelper {
    /**
     * Метод для создания id
     * @param collection
     * @return id
     */
    public static Long generateId(HashSet<StudyGroup> collection) {
        Set<Long> existingIds = new HashSet<>();
        for (StudyGroup group : collection) {
            existingIds.add(group.getId());
        }

        Random random = new Random();
        int maxAttempts = 10000;
        int attempts = 0;

        while (attempts < maxAttempts) {
            long id = System.currentTimeMillis() + random.nextInt(1000);
            if (!existingIds.contains(id)) {
                return id;
            }
            attempts++;
        }

        return System.nanoTime() + random.nextInt(1000000);
    }

    /**
     * Метод для прочтения студенческой группы с клавиатуры
     * @param scanner
     * @param collection
     */
    public static StudyGroup readStudyGroup(Scanner scanner, HashSet<StudyGroup> collection) {
        System.out.println("Введите данные группы:");

        String name = null;
        while (true) {
            System.out.print("  Название группы: ");
            name = scanner.nextLine().trim();
            if (!name.isEmpty()) {
                break;
            }
            System.out.println("  Ошибка: название не может быть пустым");
        }

        Coordinates coordinates = readCoordinates(scanner);

        long studentsCount = 0;
        while (true) {
            System.out.print("  Количество студентов (>0): ");
            try {
                studentsCount = Long.parseLong(scanner.nextLine().trim());
                if (studentsCount > 0) {
                    break;
                }
                System.out.println("  Ошибка: должно быть >0");
            } catch (NumberFormatException e) {
                System.out.println("  Ошибка: введите число");
            }
        }

        int expelledStudents = 0;
        while (true) {
            System.out.print("  Количество отчисленных (>0): ");
            try {
                expelledStudents = Integer.parseInt(scanner.nextLine().trim());
                if (expelledStudents > 0) {
                    break;
                }
                System.out.println("  Ошибка: должно быть >0");
            } catch (NumberFormatException e) {
                System.out.println("  Ошибка: введите число");
            }
        }

        FormOfEducation formOfEducation = readEnum(scanner, FormOfEducation.class, "форму обучения");

        // semesterEnum
        Semester semester = readEnum(scanner, Semester.class, "семестр");

        // groupAdmin
        Person admin = null;
        System.out.print("  Добавить администратора? (y/n): ");
        if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
            admin = readPerson(scanner);
        }

        // Создаем объект через Builder
        return new StudyGroup.Builder()
                .id(generateId(collection))
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

    /**
     * Метод для прочтения координат с клавиатуры
     * @param scanner
     */
    private static Coordinates readCoordinates(Scanner scanner) {
        Float x = null;
        while (true) {
            System.out.print("  Координата x (<=741): ");
            try {
                x = Float.parseFloat(scanner.nextLine().trim());
                if (x <= 741) {
                    break;
                }
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
                if (y > -938) {
                    break;
                }
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

    /**
     * Метод для прочтения администратора с клавиатуры
     * @param scanner
     */
    private static Person readPerson(Scanner scanner) {
        System.out.println("  Данные администратора:");

        String name = null;
        while (true) {
            System.out.print("    Имя: ");
            name = scanner.nextLine().trim();
            if (!name.isEmpty()) {
                break;
            }
            System.out.println("    Ошибка: имя не может быть пустым");
        }

        Date birthday = null;
        System.out.print("    Дата рождения год-месяц-день (yyyy-MM-dd) или пусто: ");
        String date = scanner.nextLine().trim();
        if (!date.isEmpty()) {
            try {
                birthday = java.sql.Date.valueOf(date);
            } catch (IllegalArgumentException e) {
                System.out.println("    Неверный формат, поле не будет установлено");
            }
        }

        Integer weight = null;
        while (true) {
            System.out.print("    Вес (>0): ");
            try {
                weight = Integer.parseInt(scanner.nextLine().trim());
                if (weight > 0) {
                    break;
                }
                System.out.println("    Ошибка: вес >0");
            } catch (NumberFormatException e) {
                System.out.println("    Ошибка: введите число");
            }
        }

        String passportID = null;
        while (true) {
            System.out.print("    Номер паспорта (<=20 символов): ");
            String input = scanner.nextLine().trim();

            // Проверка длины
            if (input.length() > 20) {
                System.out.println("    Ошибка: номер паспорта не может быть длиннее 20 символов");
                continue;
            }

            if (input.isEmpty()) {
                System.out.println("    Ошибка: введите номер паспорта");
                continue;
            }

            try {
                passportID = String.valueOf(Integer.parseInt(input));
                break;
            } catch (NumberFormatException e) {
                System.out.println("    Ошибка: введите число");
            }
        }

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

    /**
     * Метод для прочтения локации с клавиатуры
     * @param scanner
     */
    private static Location readLocation(Scanner scanner) {
        System.out.println("    Местоположение:");

        double x = 0;
        System.out.print("      x: ");
        try {
            x = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("      Неверный формат, установлено 0");
        }

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

    /**
     * Метод для прочтения выбора из перечислений с клавиатуры
     * @param scanner
     * @param enumClass
     * @param description
     * @param <T>
     */
    public static <T extends Enum<T>> T readEnum(Scanner scanner, Class<T> enumClass, String description) {
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