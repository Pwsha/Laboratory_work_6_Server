package org.example.client.gui.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
import java.util.Optional;

public class DialogManager {
    private final LanguageManager lang;
    private final CommandSender commandSender;
    private final TableManager tableManager;
    private final VisualizationCanvas visualizationCanvas;
    private final Runnable refreshCallback;
    private Dialog<StudyGroup> currentDialog;

    public DialogManager(LanguageManager lang, CommandSender commandSender,
                         TableManager tableManager, VisualizationCanvas visualizationCanvas,
                         Runnable refreshCallback) {
        this.lang = lang;
        this.commandSender = commandSender;
        this.tableManager = tableManager;
        this.visualizationCanvas = visualizationCanvas;
        this.refreshCallback = refreshCallback;
    }

    // Валидация с сообщениями об ошибках
    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return lang.getString("validation.name.empty");
        }
        return null;
    }

    private String validateX(String xStr) {
        try {
            float x = Float.parseFloat(xStr.trim());
            if (x > 741) {
                return lang.getString("validation.x.max") + " 741";
            }
            return null;
        } catch (NumberFormatException e) {
            return lang.getString("validation.x.number");
        }
    }

    private String validateY(String yStr) {
        try {
            long y = Long.parseLong(yStr.trim());
            if (y <= -938) {
                return lang.getString("validation.y.min") + " -938";
            }
            return null;
        } catch (NumberFormatException e) {
            return lang.getString("validation.y.number");
        }
    }

    private String validateStudentsCount(String countStr) {
        try {
            long count = Long.parseLong(countStr.trim());
            if (count <= 0) {
                return lang.getString("validation.students.positive");
            }
            return null;
        } catch (NumberFormatException e) {
            return lang.getString("validation.students.number");
        }
    }

    private String validateExpelledStudents(String expelledStr) {
        try {
            int expelled = Integer.parseInt(expelledStr.trim());
            if (expelled <= 0) {
                return lang.getString("validation.expelled.positive");
            }
            return null;
        } catch (NumberFormatException e) {
            return lang.getString("validation.expelled.number");
        }
    }

    private String validateForm(ComboBox<String> formBox) {
        if (formBox.getValue() == null) {
            return lang.getString("validation.form.empty");
        }
        return null;
    }

    private String validateSemester(ComboBox<String> semesterBox) {
        if (semesterBox.getValue() == null) {
            return lang.getString("validation.semester.empty");
        }
        return null;
    }

    public void showAddDialog() {
        createAndShowDialog(lang.getString("dialog.add.title"), null);
    }

    public void showEditDialog() {
        StudyGroupTableModel selected = tableManager.getSelectedItem();
        if (selected == null) {
            showError(lang.getString("dialog.error.no_selection"));
            return;
        }

        if (!selected.isOwnedByCurrentUser()) {
            showError(lang.getString("dialog.error.not_owner"));
            return;
        }

        createAndShowDialog(lang.getString("dialog.edit.title"), selected);
    }

    public void showDeleteDialog() {
        StudyGroupTableModel selected = tableManager.getSelectedItem();
        if (selected == null) {
            showError(lang.getString("dialog.error.no_selection"));
            return;
        }

        if (!selected.isOwnedByCurrentUser()) {
            showError(lang.getString("dialog.error.not_owner"));
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(lang.getString("dialog.delete.title"));
        confirm.setHeaderText(lang.getString("dialog.delete.header"));
        confirm.setContentText(lang.getString("dialog.delete.content") + " " + selected.getName() + "?");
        confirm.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) sendDeleteCommand(selected.getId()); });
    }

    public void showAddIfMaxDialog() {
        createAndShowDialog(lang.getString("cmd.add_if_max"), null);
    }

    public void showRemoveGreaterDialog() {
        createAndShowDialog(lang.getString("cmd.remove_greater"), null);
    }

    public void showRemoveByStudentsDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(lang.getString("cmd.remove_any_by_students_count"));
        dialog.setHeaderText(lang.getString("dialog.remove_by_students.header"));
        dialog.setContentText(lang.getString("dialog.remove_by_students.content"));

        TextField inputField = dialog.getEditor();
        inputField.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.isEmpty()) {
                try {
                    long count = Long.parseLong(val);
                    if (count <= 0) {
                        inputField.setStyle("-fx-border-color: red;");
                    } else {
                        inputField.setStyle("");
                    }
                } catch (NumberFormatException e) {
                    inputField.setStyle("-fx-border-color: red;");
                }
            } else {
                inputField.setStyle("");
            }
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                long count = Long.parseLong(result.get());
                if (count <= 0) {
                    showError(lang.getString("validation.students.positive"));
                    return;
                }
                sendRemoveByStudentsCommand(count);
            } catch (NumberFormatException e) {
                showError(lang.getString("validation.students.number"));
            }
        }
    }

    public void showCountGreaterDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(lang.getString("cmd.count_greater"));
        dialog.setHeaderText(lang.getString("dialog.count_greater.header"));
        dialog.setContentText(lang.getString("dialog.count_greater.content"));
        dialog.showAndWait().ifPresent(input -> {
            try {
                int value = Integer.parseInt(input);
                sendCountGreaterCommand(value);
            } catch (NumberFormatException e) {
                showError(lang.getString("dialog.error.invalid_number"));
            }
        });
    }

    public void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.getString("dialog.info.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lang.getString("dialog.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showObjectInfo(StudyGroup group, int currentUserId) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle(lang.getString("dialog.info.title"));
        info.setHeaderText(group.getName());

        String owner = (group.getUserId() != null && group.getUserId() == currentUserId)
                ? lang.getString("owner.self")
                : lang.getString("owner.other");

        info.setContentText(String.format(
                lang.getString("dialog.object_info.format"),
                group.getId(),
                group.getCoordinates().getX(),
                group.getCoordinates().getY(),
                group.getStudentsCount(),
                group.getExpelledStudents(),
                group.getFormOfEducation(),
                group.getSemesterEnum(),
                owner
        ));
        info.showAndWait();
    }

    public void showClearConfirm() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(lang.getString("dialog.clear.title"));
        confirm.setHeaderText(lang.getString("dialog.clear.header"));
        confirm.setContentText(lang.getString("dialog.clear.content"));
        confirm.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) sendClearCommand(); });
    }

    private void createAndShowDialog(String title, StudyGroupTableModel existing) {
        currentDialog = new Dialog<>();
        currentDialog.setTitle(title);
        currentDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        currentDialog.getDialogPane().setPrefWidth(600);
        currentDialog.getDialogPane().setMinWidth(550);
        currentDialog.getDialogPane().setMaxWidth(800);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(25));

        TextField nameField = new TextField();
        nameField.setPromptText(lang.getString("dialog.name"));

        TextField xField = new TextField();
        xField.setPromptText("X (<=741)");

        TextField yField = new TextField();
        yField.setPromptText("Y (>-938)");

        TextField studentsField = new TextField();
        studentsField.setPromptText(lang.getString("dialog.students") + " (>0)");

        TextField expelledField = new TextField();
        expelledField.setPromptText(lang.getString("dialog.expelled") + " (>0)");

        ComboBox<String> formBox = new ComboBox<>();
        formBox.getItems().addAll("FULL_TIME_EDUCATION", "DISTANCE_EDUCATION", "EVENING_CLASSES");
        formBox.setPromptText(lang.getString("dialog.form"));

        ComboBox<String> semesterBox = new ComboBox<>();
        semesterBox.getItems().addAll("FIRST", "SECOND", "THIRD", "FOURTH", "FIFTH");
        semesterBox.setPromptText(lang.getString("dialog.semester"));

        // Метки для ошибок
        Label nameError = new Label();
        nameError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        nameError.setVisible(false);

        Label xError = new Label();
        xError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        xError.setVisible(false);

        Label yError = new Label();
        yError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        yError.setVisible(false);

        Label studentsError = new Label();
        studentsError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        studentsError.setVisible(false);

        Label expelledError = new Label();
        expelledError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        expelledError.setVisible(false);

        Label formError = new Label();
        formError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        formError.setVisible(false);

        Label semesterError = new Label();
        semesterError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        semesterError.setVisible(false);

        if (existing != null) {
            nameField.setText(existing.getName());
            xField.setText(String.valueOf(existing.getX()));
            yField.setText(String.valueOf(existing.getY()));
            studentsField.setText(String.valueOf(existing.getStudentsCount()));
            expelledField.setText(String.valueOf(existing.getExpelledStudents()));
            formBox.setValue(existing.getFormOfEducation());
            semesterBox.setValue(existing.getSemesterEnum());
        }
        nameError.setWrapText(true);
        nameError.setMaxWidth(350);

        xError.setWrapText(true);
        xError.setMaxWidth(350);

        yError.setWrapText(true);
        yError.setMaxWidth(350);

        studentsError.setWrapText(true);
        studentsError.setMaxWidth(350);

        expelledError.setWrapText(true);
        expelledError.setMaxWidth(350);

        formError.setWrapText(true);
        formError.setMaxWidth(350);

        semesterError.setWrapText(true);
        semesterError.setMaxWidth(350);

        nameField.textProperty().addListener((obs, old, val) -> {
            String error = validateName(val);
            nameError.setText(error != null ? error : "");
            nameError.setVisible(error != null);
        });

        xField.textProperty().addListener((obs, old, val) -> {
            String error = validateX(val);
            xError.setText(error != null ? error : "");
            xError.setVisible(error != null);
        });

        yField.textProperty().addListener((obs, old, val) -> {
            String error = validateY(val);
            yError.setText(error != null ? error : "");
            yError.setVisible(error != null);
        });

        studentsField.textProperty().addListener((obs, old, val) -> {
            String error = validateStudentsCount(val);
            studentsError.setText(error != null ? error : "");
            studentsError.setVisible(error != null);
        });

        expelledField.textProperty().addListener((obs, old, val) -> {
            String error = validateExpelledStudents(val);
            expelledError.setText(error != null ? error : "");
            expelledError.setVisible(error != null);
        });

        formBox.valueProperty().addListener((obs, old, val) -> {
            String error = validateForm(formBox);
            formError.setText(error != null ? error : "");
            formError.setVisible(error != null);
        });

        semesterBox.valueProperty().addListener((obs, old, val) -> {
            String error = validateSemester(semesterBox);
            semesterError.setText(error != null ? error : "");
            semesterError.setVisible(error != null);
        });

        int row = 0;
        grid.add(new Label(lang.getString("dialog.name")), 0, row);
        grid.add(nameField, 1, row);
        grid.add(nameError, 2, row++);

        grid.add(new Label("X"), 0, row);
        grid.add(xField, 1, row);
        grid.add(xError, 2, row++);

        grid.add(new Label("Y"), 0, row);
        grid.add(yField, 1, row);
        grid.add(yError, 2, row++);

        grid.add(new Label(lang.getString("dialog.students")), 0, row);
        grid.add(studentsField, 1, row);
        grid.add(studentsError, 2, row++);

        grid.add(new Label(lang.getString("dialog.expelled")), 0, row);
        grid.add(expelledField, 1, row);
        grid.add(expelledError, 2, row++);

        grid.add(new Label(lang.getString("dialog.form")), 0, row);
        grid.add(formBox, 1, row);
        grid.add(formError, 2, row++);

        grid.add(new Label(lang.getString("dialog.semester")), 0, row);
        grid.add(semesterBox, 1, row);
        grid.add(semesterError, 2, row++);

        currentDialog.getDialogPane().setContent(grid);

        Button okButton = (Button) currentDialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        Runnable validateAll = () -> {
            boolean valid = validateName(nameField.getText()) == null &&
                    validateX(xField.getText()) == null &&
                    validateY(yField.getText()) == null &&
                    validateStudentsCount(studentsField.getText()) == null &&
                    validateExpelledStudents(expelledField.getText()) == null &&
                    validateForm(formBox) == null &&
                    validateSemester(semesterBox) == null;
            okButton.setDisable(!valid);
        };

        nameField.textProperty().addListener((obs, old, val) -> validateAll.run());
        xField.textProperty().addListener((obs, old, val) -> validateAll.run());
        yField.textProperty().addListener((obs, old, val) -> validateAll.run());
        studentsField.textProperty().addListener((obs, old, val) -> validateAll.run());
        expelledField.textProperty().addListener((obs, old, val) -> validateAll.run());
        formBox.valueProperty().addListener((obs, old, val) -> validateAll.run());
        semesterBox.valueProperty().addListener((obs, old, val) -> validateAll.run());

        validateAll.run();

        currentDialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

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
                showError(lang.getString("dialog.error.invalid_data"));
                return null;
            }
        });

        currentDialog.showAndWait().ifPresent(group -> {
            if (title.equals(lang.getString("dialog.add.title"))) {
                sendAddCommand(group);
            } else {
                sendUpdateCommand(existing.getId(), group);
            }
        });
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
        System.out.println("DEBUG: sendRemoveByStudentsCommand called with count=" + count);
        CommandResponse response = commandSender.sendRemoveByStudentsCount(count);

        if (response.isSuccess()) {
            refreshCallback.run();
            showInfo(response.getMessage());
        } else {
            // Локализуем сообщение об ошибке на клиенте
            String errorMessage = response.getMessage();
            String localizedError = translateRemoveError(errorMessage, count);
            showError(localizedError);
        }
    }

    private String translateRemoveError(String englishMessage, Long count) {
        if (englishMessage.contains("not found")) {
            return lang.getString("dialog.remove_by_students.not_found") + ": " + count;
        }
        if (englishMessage.contains("Database error") || englishMessage.contains("error")) {
            return lang.getString("dialog.remove_by_students.db_error");
        }
        return englishMessage;
    }

    private void sendCountGreaterCommand(Integer value) {
        commandSender.sendCountGreater(value);
    }

    private void sendClearCommand() {
        commandSender.send(CommandType.CLEAR);
        refreshCallback.run();
    }
}