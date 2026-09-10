package org.example.client.gui;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.client.Client;
import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.common.init.StudyGroup;
import org.example.common.init.Coordinates;
import org.example.common.init.FormOfEducation;
import org.example.common.init.Semester;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {
    private final Stage stage;
    private final Client client;
    private String authToken;
    private String username;
    private int currentUserId;
    private final LanguageManager lang;

    private TableView<StudyGroupTableModel> tableView;
    private ObservableList<StudyGroupTableModel> tableData;
    private FilteredList<StudyGroupTableModel> filteredData;
    private VisualizationCanvas visualizationCanvas;
    private Label userLabel;
    private Timeline autoRefreshTimeline;
    private ComboBox<String> filterComboBox;
    private TextField filterTextField;
    private ComboBox<String> sortComboBox;
    private ComboBox<String> languageSelector;

    public MainController(Stage stage, Client client, String authToken, String username, int currentUserId) {
        this.stage = stage;
        this.client = client;
        this.authToken = authToken;
        this.username = username;
        this.currentUserId = currentUserId;
        this.lang = LanguageManager.getInstance();
    }

    public void show() {
        initUI();
        refreshTable();
        startAutoRefresh();

        AnimationManager.fadeIn(stage.getScene().getRoot(), Duration.millis(500));
    }

    public void switchUser(String newAuthToken, String newUsername, int newUserId) {
        this.authToken = newAuthToken;
        this.username = newUsername;
        this.currentUserId = newUserId;

        stage.setTitle(lang.getString("app.title") + " - " + username);
        if (userLabel != null) {
            userLabel.setText(lang.getString("main.user") + " " + username);
        }

        refreshTable();
    }

    private void initUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f0f0;");
        root.setTop(createToolbar());

        SplitPane centerSplit = new SplitPane();
        centerSplit.setDividerPositions(0.6);
        centerSplit.getItems().addAll(createTableView(), createVisualizationView());
        root.setCenter(centerSplit);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setTitle(lang.getString("app.title") + " - " + username);
        stage.setScene(scene);
        stage.show();
    }

    private ToolBar createToolbar() {
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: #2b3b4c; -fx-padding: 10;");

        userLabel = new Label(lang.getString("main.user") + " " + username);
        userLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Button refreshBtn = new Button(lang.getString("main.refresh"));
        refreshBtn.setOnAction(e -> {
            AnimationManager.pulse(refreshBtn);
            refreshTable();
        });

        Button addBtn = new Button(lang.getString("main.add"));
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addBtn.setOnAction(e -> {
            AnimationManager.pulse(addBtn);
            showAddDialog();
        });

        Button editBtn = new Button(lang.getString("main.edit"));
        editBtn.setOnAction(e -> {
            AnimationManager.pulse(editBtn);
            editSelectedObject();
        });

        Button deleteBtn = new Button(lang.getString("main.delete"));
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            AnimationManager.pulse(deleteBtn);
            deleteSelectedObject();
        });

        Button logoutBtn = new Button(lang.getString("main.logout"));
        logoutBtn.setOnAction(e -> {
            AnimationManager.fadeOut(toolbar, Duration.millis(200), () -> logout());
        });

        languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll("Русский", "English (AU)", "Nederlands", "Svenska");
        languageSelector.setValue("Русский");
        languageSelector.setOnAction(e -> changeLanguage());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getItems().addAll(userLabel, refreshBtn, addBtn, editBtn, deleteBtn, spacer, languageSelector, logoutBtn);
        return toolbar;
    }

    private VBox createTableView() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        HBox filterBox = new HBox(10);
        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("ID", lang.getString("table.name"), lang.getString("table.studentsCount"));
        filterComboBox.setValue(lang.getString("table.name"));

        filterTextField = new TextField();
        filterTextField.setPromptText(lang.getString("main.filter"));
        filterTextField.textProperty().addListener((obs, old, val) -> applyFilter());

        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("ID", lang.getString("table.name"), lang.getString("table.studentsCount"));
        sortComboBox.setValue("ID");
        sortComboBox.setOnAction(e -> applySort());

        filterBox.getChildren().addAll(new Label(lang.getString("main.filter")), filterComboBox, filterTextField, new Label(lang.getString("main.sort")), sortComboBox);

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<StudyGroupTableModel, Long> idCol = new TableColumn<>(lang.getString("table.id"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<StudyGroupTableModel, String> nameCol = new TableColumn<>(lang.getString("table.name"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<StudyGroupTableModel, Float> xCol = new TableColumn<>("X");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        xCol.setPrefWidth(60);

        TableColumn<StudyGroupTableModel, Long> yCol = new TableColumn<>("Y");
        yCol.setCellValueFactory(new PropertyValueFactory<>("y"));
        yCol.setPrefWidth(60);

        TableColumn<StudyGroupTableModel, LocalDateTime> dateCol = new TableColumn<>(lang.getString("table.creationDate"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        dateCol.setCellFactory(col -> new TableCell<StudyGroupTableModel, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
        dateCol.setPrefWidth(150);

        TableColumn<StudyGroupTableModel, Long> studentsCol = new TableColumn<>(lang.getString("table.studentsCount"));
        studentsCol.setCellValueFactory(new PropertyValueFactory<>("studentsCount"));
        studentsCol.setPrefWidth(100);

        TableColumn<StudyGroupTableModel, Integer> expelledCol = new TableColumn<>(lang.getString("table.expelledStudents"));
        expelledCol.setCellValueFactory(new PropertyValueFactory<>("expelledStudents"));
        expelledCol.setPrefWidth(80);

        TableColumn<StudyGroupTableModel, String> formCol = new TableColumn<>(lang.getString("table.formOfEducation"));
        formCol.setCellValueFactory(new PropertyValueFactory<>("formOfEducation"));
        formCol.setPrefWidth(150);

        TableColumn<StudyGroupTableModel, String> semCol = new TableColumn<>(lang.getString("table.semester"));
        semCol.setCellValueFactory(new PropertyValueFactory<>("semesterEnum"));
        semCol.setPrefWidth(80);

        tableView.getColumns().addAll(idCol, nameCol, xCol, yCol, dateCol, studentsCol, expelledCol, formCol, semCol);
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                AnimationManager.pulse(tableView);
                editSelectedObject();
            }
        });

        box.getChildren().addAll(filterBox, tableView);
        return box;
    }

    private VBox createVisualizationView() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #2b3b4c;");

        Label titleLabel = new Label(lang.getString("visualization.title"));
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        HBox legendBox = new HBox(20);
        legendBox.setAlignment(javafx.geometry.Pos.CENTER);

        HBox ownBox = new HBox(5);
        javafx.scene.shape.Circle greenCircle = new javafx.scene.shape.Circle(8, javafx.scene.paint.Color.GREEN);
        Label ownLabel = new Label("Мои объекты");
        ownLabel.setStyle("-fx-text-fill: white;");
        ownBox.getChildren().addAll(greenCircle, ownLabel);

        HBox otherBox = new HBox(5);
        javafx.scene.shape.Circle redCircle = new javafx.scene.shape.Circle(8, javafx.scene.paint.Color.RED);
        Label otherLabel = new Label("Объекты других пользователей");
        otherLabel.setStyle("-fx-text-fill: white;");
        otherBox.getChildren().addAll(redCircle, otherLabel);

        legendBox.getChildren().addAll(ownBox, otherBox);

        visualizationCanvas = new VisualizationCanvas(500, 500);
        visualizationCanvas.setOnObjectClick(this::showObjectInfo);
        visualizationCanvas.setOnObjectDoubleClick(this::editObject);

        box.getChildren().addAll(titleLabel, legendBox, visualizationCanvas);
        return box;
    }

    private void refreshTable() {
        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.SHOW)
                .stringArg(authToken)
                .build();
        CommandResponse response = client.sendRequest(request);

        if (response.isSuccess() && response.getCollection() != null) {
            List<StudyGroupTableModel> models = response.getCollection().stream()
                    .filter(g -> g != null && g.getId() != null)
                    .map(g -> StudyGroupTableModel.from(g, currentUserId))
                    .collect(Collectors.toList());

            tableData = FXCollections.observableArrayList(models);
            filteredData = new FilteredList<>(tableData, p -> true);
            applyFilter();
            applySort();

            visualizationCanvas.updateObjects(response.getCollection(), currentUserId);
        }
    }

    private void applyFilter() {
        if (filteredData == null) return;
        String filterColumn = filterComboBox.getValue();
        String filterText = filterTextField.getText().toLowerCase();
        String nameLabel = lang.getString("table.name");
        String studentsLabel = lang.getString("table.studentsCount");

        if (filterText == null || filterText.isEmpty()) {
            filteredData.setPredicate(p -> true);
        } else {
            filteredData.setPredicate(item -> {
                if (filterColumn.equals("ID")) {
                    return String.valueOf(item.getId()).contains(filterText);
                } else if (filterColumn.equals(nameLabel)) {
                    return item.getName().toLowerCase().contains(filterText);
                } else if (filterColumn.equals(studentsLabel)) {
                    return String.valueOf(item.getStudentsCount()).contains(filterText);
                }
                return true;
            });
        }
        applySort();
    }

    private void applySort() {
        if (filteredData == null) return;
        String sortColumn = sortComboBox.getValue();
        String nameLabel = lang.getString("table.name");
        String studentsLabel = lang.getString("table.studentsCount");

        java.util.Comparator<StudyGroupTableModel> comparator;
        if (sortColumn.equals("ID")) {
            comparator = java.util.Comparator.comparing(StudyGroupTableModel::getId);
        } else if (sortColumn.equals(nameLabel)) {
            comparator = java.util.Comparator.comparing(StudyGroupTableModel::getName);
        } else if (sortColumn.equals(studentsLabel)) {
            comparator = java.util.Comparator.comparingLong(StudyGroupTableModel::getStudentsCount);
        } else {
            comparator = java.util.Comparator.comparing(StudyGroupTableModel::getId);
        }
        tableView.setItems(new SortedList<>(filteredData, comparator));
    }

    private void showAddDialog() {
        Dialog<StudyGroup> dialog = createGroupDialog(lang.getString("dialog.add.title"), null);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? dialog.getResult() : null);
        dialog.setOnShown(e -> AnimationManager.fadeIn(dialog.getDialogPane(), Duration.millis(300)));
        dialog.showAndWait().ifPresent(this::sendAddCommand);
    }

    private void editSelectedObject() {
        StudyGroupTableModel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AnimationManager.shake(tableView);
            showError("Выберите элемент для редактирования");
            return;
        }

        if (!selected.isOwnedByCurrentUser()) {
            AnimationManager.shake(tableView);
            showError("Вы можете редактировать только свои объекты");
            return;
        }

        Dialog<StudyGroup> dialog = createEditDialog(selected);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? dialog.getResult() : null);
        dialog.showAndWait().ifPresent(newGroup -> sendUpdateCommand(selected.getId(), newGroup));
    }

    private void editObject(StudyGroup group) {
        StudyGroupTableModel model = tableData.stream()
                .filter(m -> m.getId() == group.getId())
                .findFirst()
                .orElse(null);

        if (model != null && model.isOwnedByCurrentUser()) {
            Dialog<StudyGroup> dialog = createEditDialog(model);
            dialog.setResultConverter(btn -> btn == ButtonType.OK ? dialog.getResult() : null);
            dialog.showAndWait().ifPresent(newGroup -> sendUpdateCommand(model.getId(), newGroup));
        } else if (model != null) {
            AnimationManager.shake(tableView);
            showError("Вы можете редактировать только свои объекты");
        }
    }

    private void deleteSelectedObject() {
        StudyGroupTableModel selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AnimationManager.shake(tableView);
            showError("Выберите элемент для удаления");
            return;
        }

        if (!selected.isOwnedByCurrentUser()) {
            AnimationManager.shake(tableView);
            showError("Вы можете удалять только свои объекты");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить объект " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) sendDeleteCommand(selected.getId());
        });
    }

    private void sendAddCommand(StudyGroup group) {
        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.ADD)
                .stringArg(authToken)
                .studyGroup(group)
                .build();
        CommandResponse response = client.sendRequest(request);
        if (response.isSuccess()) {
            refreshTable();
        } else {
            AnimationManager.shake(tableView);
            showError(response.getMessage());
        }
    }

    private void sendUpdateCommand(Long id, StudyGroup group) {
        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.UPDATE)
                .stringArg(authToken)
                .id(id)
                .studyGroup(group)
                .build();
        CommandResponse response = client.sendRequest(request);
        if (response.isSuccess()) {
            refreshTable();
        } else {
            AnimationManager.shake(tableView);
            showError(response.getMessage());
        }
    }

    private void sendDeleteCommand(Long id) {
        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.REMOVE_BY_ID)
                .stringArg(authToken)
                .id(id)
                .build();
        CommandResponse response = client.sendRequest(request);
        if (response.isSuccess()) {
            refreshTable();
        } else {
            AnimationManager.shake(tableView);
            showError(response.getMessage());
        }
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
        TextField xField = new TextField();
        TextField yField = new TextField();
        TextField studentsField = new TextField();
        TextField expelledField = new TextField();
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

        grid.add(new Label(lang.getString("dialog.name")), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("X"), 0, 1);
        grid.add(xField, 1, 1);
        grid.add(new Label("Y"), 0, 2);
        grid.add(yField, 1, 2);
        grid.add(new Label(lang.getString("dialog.students")), 0, 3);
        grid.add(studentsField, 1, 3);
        grid.add(new Label(lang.getString("dialog.expelled")), 0, 4);
        grid.add(expelledField, 1, 4);
        grid.add(new Label(lang.getString("dialog.form")), 0, 5);
        grid.add(formBox, 1, 5);
        grid.add(new Label(lang.getString("dialog.semester")), 0, 6);
        grid.add(semesterBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Coordinates coords = new Coordinates.Builder()
                            .x(Float.parseFloat(xField.getText()))
                            .y(Long.parseLong(yField.getText()))
                            .build();
                    return new StudyGroup.Builder()
                            .name(nameField.getText())
                            .coordinates(coords)
                            .studentsCount(Long.parseLong(studentsField.getText()))
                            .expelledStudents(Integer.parseInt(expelledField.getText()))
                            .formOfEducation(FormOfEducation.valueOf(formBox.getValue()))
                            .semesterEnum(Semester.valueOf(semesterBox.getValue()))
                            .creationDate(LocalDateTime.now())
                            .build();
                } catch (Exception e) {
                    AnimationManager.shake(dialog.getDialogPane());
                    showError("Ошибка ввода данных");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private Dialog<StudyGroup> createEditDialog(StudyGroupTableModel existing) {
        return createGroupDialog(lang.getString("dialog.edit.title"), existing);
    }

    private void showObjectInfo(StudyGroup group) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Информация");
        info.setHeaderText(group.getName());
        info.setContentText(String.format(
                "ID: %d\nX: %.1f, Y: %d\nСтудентов: %d\nОтчислено: %d\nФорма: %s\nСеместр: %s\nВладелец: %s",
                group.getId(),
                group.getCoordinates().getX(),
                group.getCoordinates().getY(),
                group.getStudentsCount(),
                group.getExpelledStudents(),
                group.getFormOfEducation(),
                group.getSemesterEnum(),
                group.getUserId() == currentUserId ? "Вы" : "Другой пользователь"
        ));
        AnimationManager.fadeIn(info.getDialogPane(), Duration.millis(200));
        info.showAndWait();
    }

    private void changeLanguage() {
        String selected = languageSelector.getValue();
        if (selected.contains("English")) lang.setLocale("en_AU");
        else if (selected.contains("Nederlands")) lang.setLocale("nl");
        else if (selected.contains("Svenska")) lang.setLocale("sv");
        else lang.setLocale("ru");

        refreshTable();
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> refreshTable()));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void logout() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();

        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.LOGOUT)
                .stringArg(authToken)
                .build();
        client.sendRequest(request);
        client.disconnect();

        AnimationManager.fadeOut(stage.getScene().getRoot(), Duration.millis(300), () -> {
            stage.close();
            new AuthController(new Stage(), client);
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        AnimationManager.shake(alert.getDialogPane());
        alert.showAndWait();
    }
}