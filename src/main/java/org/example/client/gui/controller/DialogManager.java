package org.example.client.gui.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.example.client.gui.AnimationManager;
import org.example.client.gui.LanguageManager;
import org.example.client.gui.StudyGroupTableModel;
import org.example.client.gui.VisualizationCanvas;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.common.init.StudyGroup;
import org.example.common.init.Coordinates;
import org.example.common.init.FormOfEducation;
import org.example.common.init.Semester;

import java.time.LocalDateTime;

public class DialogManager {
    private final LanguageManager lang;
    private final CommandSender commandSender;
    private final TableManager tableManager;
    private final Runnable refreshCallback;

    public DialogManager(LanguageManager lang, CommandSender commandSender,
                         TableManager tableManager, VisualizationCanvas visualizationCanvas,
                         Runnable refreshCallback) {
        this.lang = lang;
        this.commandSender = commandSender;
        this.tableManager = tableManager;
        this.refreshCallback = refreshCallback;
    }

    public void showAddDialog() {
        Dialog<StudyGroup> dialog = createGroupDialog(lang.getString("dialog.add.title"), null);
        dialog.showAndWait().ifPresent(this::sendAddCommand);
    }

    public void showEditDialog() {
        StudyGroupTableModel selected = tableManager.getSelectedItem();
        if (selected == null) {
            showError("Выберите элемент для редактирования");
            return;
        }

        if (!selected.isOwnedByCurrentUser()) {
            showError("Вы можете редактировать только свои объекты");
            return;
        }

        Dialog<StudyGroup> dialog = createEditDialog(selected);
        dialog.showAndWait().ifPresent(newGroup -> sendUpdateCommand(selected.getId(), newGroup));
    }

    public void showDeleteDialog() {
        StudyGroupTableModel selected = tableManager.getSelectedItem();
        if (selected == null) {
            showError("Выберите элемент для удаления");
            return;
        }

        if (!selected.isOwnedByCurrentUser()) {
            showError("Вы можете удалять только свои объекты");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удаление объекта");
        confirm.setContentText("Удалить объект " + selected.getName() + "?");
        confirm.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) sendDeleteCommand(selected.getId()); });
    }

    public void showAddIfMaxDialog() {
        createGroupDialog(lang.getString("cmd.add_if_max"), null).showAndWait().ifPresent(this::sendAddIfMaxCommand);
    }

    public void showRemoveGreaterDialog() {
        createGroupDialog(lang.getString("cmd.remove_greater"), null).showAndWait().ifPresent(this::sendRemoveGreaterCommand);
    }

    public void showRemoveByStudentsDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(lang.getString("cmd.remove_any_by_students_count"));
        dialog.setHeaderText("Введите количество студентов");
        dialog.showAndWait().ifPresent(input -> {
            try { sendRemoveByStudentsCommand(Long.parseLong(input)); }
            catch (NumberFormatException e) { showError("Введите число"); }
        });
    }

    public void showCountGreaterDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(lang.getString("cmd.count_greater"));
        dialog.setHeaderText("Введите количество отчисленных");
        dialog.showAndWait().ifPresent(input -> {
            try { sendCountGreaterCommand(Integer.parseInt(input)); }
            catch (NumberFormatException e) { showError("Введите число"); }
        });
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lang.getString("dialog.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        AnimationManager.shake(alert.getDialogPane());
        alert.showAndWait();
    }

    public void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.getString("dialog.info.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        AnimationManager.fadeIn(alert.getDialogPane(), Duration.millis(200));
        alert.showAndWait();
    }

    public void showObjectInfo(StudyGroup group, int currentUserId) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Информация");
        info.setHeaderText(group.getName());
        String owner = (group.getUserId() != null && group.getUserId() == currentUserId) ? "Вы" : "Другой пользователь";
        info.setContentText(String.format(
                "ID: %d\nX: %.1f, Y: %d\nСтудентов: %d\nОтчислено: %d\nФорма: %s\nСеместр: %s\nВладелец: %s",
                group.getId(), group.getCoordinates().getX(), group.getCoordinates().getY(),
                group.getStudentsCount(), group.getExpelledStudents(),
                group.getFormOfEducation(), group.getSemesterEnum(), owner));
        info.showAndWait();
    }

    public void showClearConfirm() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Очистка коллекции");
        confirm.setContentText("Вы уверены? Действие необратимо!");
        confirm.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) sendClearCommand(); });
    }

    private void sendAddCommand(StudyGroup group) {
        CommandResponse response = commandSender.send(CommandType.ADD, group);
        if (response.isSuccess()) refreshCallback.run();
        else showError(response.getMessage());
    }

    private void sendUpdateCommand(Long id, StudyGroup group) {
        CommandResponse response = commandSender.sendUpdate(id, group);
        if (response.isSuccess()) refreshCallback.run();
        else showError(response.getMessage());
    }

    private void sendDeleteCommand(Long id) {
        commandSender.sendRemoveById(id);
        refreshCallback.run();
    }

    private void sendAddIfMaxCommand(StudyGroup group) {
        commandSender.send(CommandType.ADD_IF_MAX, group);
        refreshCallback.run();
    }

    private void sendRemoveGreaterCommand(StudyGroup group) {
        commandSender.send(CommandType.REMOVE_GREATER, group);
        refreshCallback.run();
    }

    private void sendRemoveByStudentsCommand(Long count) {
        commandSender.send(CommandType.REMOVE_ANY_BY_STUDENTS_COUNT, count);
        refreshCallback.run();
    }

    private void sendCountGreaterCommand(Integer value) {
        commandSender.send(CommandType.COUNT_GREATER_THAN_EXPELLED_STUDENTS, value);
    }

    private void sendClearCommand() {
        commandSender.send(CommandType.CLEAR);
        refreshCallback.run();
    }

    private Dialog<StudyGroup> createGroupDialog(String title, StudyGroupTableModel existing) {
        Dialog<StudyGroup> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Название");
        TextField xField = new TextField();
        xField.setPromptText("X");
        TextField yField = new TextField();
        yField.setPromptText("Y");
        TextField studentsField = new TextField();
        studentsField.setPromptText("Студенты");
        TextField expelledField = new TextField();
        expelledField.setPromptText("Отчисленные");

        ComboBox<String> formBox = new ComboBox<>();
        formBox.getItems().addAll("FULL_TIME_EDUCATION", "DISTANCE_EDUCATION", "EVENING_CLASSES");
        ComboBox<String> semesterBox = new ComboBox<>();
        semesterBox.getItems().addAll("FIRST", "SECOND", "THIRD", "FOURTH", "FIFTH");

        if (existing != null) {
            nameField.setText(existing.getName());
            xField.setText(String.valueOf(existing.getX()));
            yField.setText(String.valueOf(existing.getY()));
            studentsField.setText(String.valueOf(existing.getStudentsCount()));
            expelledField.setText(String.valueOf(existing.getExpelledStudents()));
            formBox.setValue(existing.getFormOfEducation());
            semesterBox.setValue(existing.getSemesterEnum());
        }

        int row = 0;
        grid.add(new Label(lang.getString("dialog.name")), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("X"), 0, row);
        grid.add(xField, 1, row++);
        grid.add(new Label("Y"), 0, row);
        grid.add(yField, 1, row++);
        grid.add(new Label(lang.getString("dialog.students")), 0, row);
        grid.add(studentsField, 1, row++);
        grid.add(new Label(lang.getString("dialog.expelled")), 0, row);
        grid.add(expelledField, 1, row++);
        grid.add(new Label(lang.getString("dialog.form")), 0, row);
        grid.add(formBox, 1, row++);
        grid.add(new Label(lang.getString("dialog.semester")), 0, row);
        grid.add(semesterBox, 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            if (nameField.getText().trim().isEmpty()) {
                showError("Введите название");
                return null;
            }
            try {
                Coordinates coords = new Coordinates.Builder()
                        .x(Float.parseFloat(xField.getText().trim()))
                        .y(Long.parseLong(yField.getText().trim()))
                        .build();
                return new StudyGroup.Builder()
                        .name(nameField.getText().trim())
                        .coordinates(coords)
                        .studentsCount(Long.parseLong(studentsField.getText().trim()))
                        .expelledStudents(Integer.parseInt(expelledField.getText().trim()))
                        .formOfEducation(FormOfEducation.valueOf(formBox.getValue()))
                        .semesterEnum(Semester.valueOf(semesterBox.getValue()))
                        .creationDate(LocalDateTime.now())
                        .build();
            } catch (Exception e) {
                showError("Ошибка ввода: " + e.getMessage());
                return null;
            }
        });
        return dialog;
    }

    private Dialog<StudyGroup> createEditDialog(StudyGroupTableModel selected) {
        return createGroupDialog(lang.getString("dialog.edit.title"), selected);
    }
}