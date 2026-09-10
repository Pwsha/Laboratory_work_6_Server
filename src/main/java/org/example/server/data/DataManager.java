package org.example.server.data;

import org.example.common.init.StudyGroup;
import org.example.common.init.Coordinates;
import org.example.common.init.FormOfEducation;
import org.example.common.init.Semester;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;

public class DataManager {
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
        }
    }

    public DataManager() {
        Properties props = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Ошибка: Файл config.properties не найден!");
            }

            props.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки config.properties: " + e.getMessage(), e);
        }

        this.host = props.getProperty("db.host", "localhost");
        this.port = Integer.parseInt(props.getProperty("db.port", "5432"));
        this.database = props.getProperty("db.name", "studs");
        this.user = props.getProperty("db.user", "postgres");
        this.password = props.getProperty("db.password");

        if (this.password == null || this.password.isEmpty()) {
            System.err.println("Ошибка: Пароль не найден в config.properties!");
        }

        String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);

        try {
            this.connection = DriverManager.getConnection(url, user, password);
            System.out.println("Подключение к БД успешно!");
            System.out.println("Хост: " + host);
            System.out.println("Порт: " + port);
            System.out.println("База: " + database);
            System.out.println("Пользователь: " + user);
            initTables();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения к БД: " + e.getMessage(), e);
        }
    }

    private void initTables() throws SQLException {
        String createSequence = """
            CREATE SEQUENCE IF NOT EXISTS study_group_id_seq START WITH 1 INCREMENT BY 1
        """;

        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                login VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(56) NOT NULL
            )
        """;

        String createStudyGroupTable = """
            CREATE TABLE IF NOT EXISTS study_group (
                id BIGINT PRIMARY KEY DEFAULT nextval('study_group_id_seq'),
                name VARCHAR(255) NOT NULL,
                coordinates_x FLOAT NOT NULL,
                coordinates_y BIGINT NOT NULL,
                creation_date TIMESTAMP NOT NULL,
                students_count BIGINT NOT NULL,
                expelled_students INTEGER NOT NULL,
                form_of_education VARCHAR(50) NOT NULL,
                semester_enum VARCHAR(50) NOT NULL,
                user_id INTEGER REFERENCES users(id) ON DELETE CASCADE
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSequence);
            stmt.execute(createUsersTable);
            stmt.execute(createStudyGroupTable);
            System.out.println("Таблицы инициализированы");
        }
    }

    public boolean registerUser(String login, String hashedPassword) {
        String sql = "INSERT INTO users (login, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, hashedPassword);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка регистрации: " + e.getMessage());
            return false;
        }
    }

    public Optional<Integer> authenticate(String login, String hashedPassword) {
        String sql = "SELECT id FROM users WHERE login = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, hashedPassword);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                System.out.println("DEBUG: Found userId=" + userId + " for login=" + login);
                return Optional.of(userId);
            }
            System.out.println("DEBUG: No user found for login=" + login);
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("Auth error: " + e.getMessage());
            return Optional.empty();
        }
    }

    public HashSet<StudyGroup> loadAllGroups() {
        HashSet<StudyGroup> groups = new HashSet<>();
        String sql = "SELECT * FROM study_group";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки групп: " + e.getMessage());
        }
        return groups;
    }

    // В DatabaseManager.java
    public Long addGroup(StudyGroup group, int userId) {
        String sql = "INSERT INTO study_group (name, coordinates_x, coordinates_y, creation_date, students_count, expelled_students, form_of_education, semester_enum, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, group.getName());
            stmt.setDouble(2, group.getCoordinates().getX());
            stmt.setLong(3, group.getCoordinates().getY());
            stmt.setTimestamp(4, Timestamp.valueOf(group.getCreationDate()));
            stmt.setLong(5, group.getStudentsCount());
            stmt.setInt(6, group.getExpelledStudents());
            stmt.setString(7, group.getFormOfEducation().name());
            stmt.setString(8, group.getSemesterEnum().name());
            stmt.setInt(9, userId);  // ← УБЕДИТЕСЬ, ЧТО ЭТА СТРОКА ЕСТЬ!

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long generatedId = rs.getLong(1);
                group.setId(generatedId);
                group.setUserId(userId);
                return generatedId;
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean updateGroup(Long id, StudyGroup newGroup, int userId) {
        String sql = "UPDATE study_group SET name=?, coordinates_x=?, coordinates_y=?, creation_date=?, students_count=?, expelled_students=?, form_of_education=?, semester_enum=?, user_id=? WHERE id=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newGroup.getName());
            stmt.setDouble(2, newGroup.getCoordinates().getX());
            stmt.setLong(3, newGroup.getCoordinates().getY());
            stmt.setTimestamp(4, Timestamp.valueOf(newGroup.getCreationDate()));
            stmt.setLong(5, newGroup.getStudentsCount());
            stmt.setInt(6, newGroup.getExpelledStudents());
            stmt.setString(7, newGroup.getFormOfEducation().name());
            stmt.setString(8, newGroup.getSemesterEnum().name());
            stmt.setInt(9, userId);
            stmt.setLong(10, id);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteGroup(Long id, int userId) {
        String sql = "DELETE FROM study_group WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка удаления группы: " + e.getMessage());
            return false;
        }
    }

    public boolean clearGroups(int userId) {
        String countSql = "SELECT COUNT(*) FROM study_group WHERE user_id = ?";
        try (PreparedStatement countStmt = connection.prepareStatement(countSql)) {
            countStmt.setInt(1, userId);
            ResultSet rs = countStmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count == 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при подсчёте: " + e.getMessage());
        }

        String sql = "DELETE FROM study_group WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rows = stmt.executeUpdate();
            return rows >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean groupExists(Long id, int userId) {
        String sql = "SELECT 1 FROM study_group WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private StudyGroup mapResultSetToGroup(ResultSet rs) throws SQLException {
        Coordinates coordinates = new Coordinates.Builder()
                .x((float) rs.getDouble("coordinates_x"))
                .y(rs.getLong("coordinates_y"))
                .build();

        return new StudyGroup.Builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .coordinates(coordinates)
                .creationDate(rs.getTimestamp("creation_date").toLocalDateTime())
                .studentsCount(rs.getLong("students_count"))
                .expelledStudents(rs.getInt("expelled_students"))
                .formOfEducation(FormOfEducation.valueOf(rs.getString("form_of_education")))
                .semesterEnum(Semester.valueOf(rs.getString("semester_enum")))
                .userId(rs.getInt("user_id"))
                .build();
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Соединение с БД закрыто");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка закрытия соединения: " + e.getMessage());
        }
    }
}