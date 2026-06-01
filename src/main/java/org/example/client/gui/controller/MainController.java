package org.example.client.gui.controller;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.client.Client;
import org.example.client.gui.*;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.common.init.StudyGroup;

public class MainController {
    private final Stage stage;
    private final Client client;
    private final LanguageManager lang;
    private final CommandSender commandSender;
    private final TableManager tableManager;
    private final DialogManager dialogManager;
    private final VisualizationCanvas visualizationCanvas;

    private String authToken;
    private String username;
    private int currentUserId;
    private Timeline autoRefreshTimeline;
    private Label userLabel;
    private ComboBox<String> languageSelector;

    // Ссылки на кнопки для обновления текста
    private Button refreshBtn, addBtn, editBtn, deleteBtn, infoBtn, clearBtn;
    private Button addIfMaxBtn, removeGreaterBtn, removeByStudentsBtn, minBySemesterBtn, countGreaterBtn, logoutBtn;

    public MainController(Stage stage, Client client, String authToken, String username, int currentUserId) {
        this.stage = stage;
        this.client = client;
        this.authToken = authToken;
        this.username = username;
        this.currentUserId = currentUserId;
        this.lang = LanguageManager.getInstance();

        this.commandSender = new CommandSender(client, authToken);
        this.tableManager = new TableManager(lang);
        this.visualizationCanvas = new VisualizationCanvas(500, 500);
        this.dialogManager = new DialogManager(lang, commandSender, tableManager, visualizationCanvas, this::refreshTable);

        visualizationCanvas.setOnObjectClick(this::showObjectInfo);
        visualizationCanvas.setOnObjectDoubleClick(group -> {
            StudyGroupTableModel model = tableManager.getTableView().getItems().stream()
                    .filter(m -> m.getId() == group.getId())
                    .findFirst().orElse(null);
            if (model != null && model.isOwnedByCurrentUser()) {
                dialogManager.showEditDialog();
                AnimationManager.pulse(tableManager.getTableView());
            }
        });

        lang.addLocaleChangeListener(locale -> refreshUI());
    }

    public void show() {
        initUI();
        refreshTable();
        startAutoRefresh();
        AnimationManager.fadeIn(stage.getScene().getRoot(), Duration.millis(500));
    }

    public void close() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
        stage.close();
    }

    private void initUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f0f0;");
        root.setTop(createToolbar());
        root.setCenter(createCenterPane());

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

        refreshBtn = createButtonWithAnimation("main.refresh", e -> refreshTable());
        addBtn = createStyledButtonWithAnimation("main.add", "#4CAF50", e -> dialogManager.showAddDialog());
        editBtn = createButtonWithAnimation("main.edit", e -> dialogManager.showEditDialog());
        deleteBtn = createStyledButtonWithAnimation("main.delete", "#f44336", e -> dialogManager.showDeleteDialog());

        infoBtn = createButtonWithAnimation("cmd.info", e -> sendInfoCommand());
        clearBtn = createButtonWithAnimation("cmd.clear", e -> sendClearCommand());
        addIfMaxBtn = createButtonWithAnimation("cmd.add_if_max", e -> dialogManager.showAddIfMaxDialog());
        removeGreaterBtn = createButtonWithAnimation("cmd.remove_greater", e -> dialogManager.showRemoveGreaterDialog());
        removeByStudentsBtn = createButtonWithAnimation("cmd.remove_any_by_students_count", e -> dialogManager.showRemoveByStudentsDialog());
        minBySemesterBtn = createButtonWithAnimation("cmd.min_by_semester_enum", e -> sendMinBySemesterCommand());
        countGreaterBtn = createButtonWithAnimation("cmd.count_greater", e -> dialogManager.showCountGreaterDialog());

        logoutBtn = createButtonWithAnimation("main.logout", e -> logout());

        languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll("Русский", "English (AU)", "Nederlands", "Svenska");
        languageSelector.setValue("Русский");
        languageSelector.setOnAction(e -> changeLanguage());

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        toolbar.getItems().addAll(
                userLabel, refreshBtn, addBtn, editBtn, deleteBtn,
                spacer1, infoBtn, clearBtn, addIfMaxBtn, removeGreaterBtn,
                removeByStudentsBtn, minBySemesterBtn, countGreaterBtn,
                spacer2, languageSelector, logoutBtn
        );
        return toolbar;
    }

    private Button createButtonWithAnimation(String key, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(lang.getString(key));
        btn.setOnAction(e -> {
            AnimationManager.pulse(btn);
            handler.handle(e);
        });
        return btn;
    }

    private Button createStyledButtonWithAnimation(String key, String color, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = createButtonWithAnimation(key, handler);
        if (color != null) btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        return btn;
    }

    private SplitPane createCenterPane() {
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.6);

        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(10));
        tableBox.getChildren().addAll(createFilterPanel(), tableManager.getTableView());

        VBox vizBox = new VBox(10);
        vizBox.setPadding(new Insets(10));
        vizBox.setStyle("-fx-background-color: #2b3b4c;");

        Label titleLabel = new Label(lang.getString("visualization.title"));
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        HBox legendBox = new HBox(20);
        legendBox.setAlignment(javafx.geometry.Pos.CENTER);

        HBox ownBox = new HBox(5);
        javafx.scene.shape.Circle greenCircle = new javafx.scene.shape.Circle(8, javafx.scene.paint.Color.GREEN);
        Label ownLabel = new Label(lang.getString("visualization.own"));
        ownLabel.setStyle("-fx-text-fill: white;");
        ownBox.getChildren().addAll(greenCircle, ownLabel);

        HBox otherBox = new HBox(5);
        javafx.scene.shape.Circle redCircle = new javafx.scene.shape.Circle(8, javafx.scene.paint.Color.RED);
        Label otherLabel = new Label(lang.getString("visualization.other"));
        otherLabel.setStyle("-fx-text-fill: white;");
        otherBox.getChildren().addAll(redCircle, otherLabel);

        legendBox.getChildren().addAll(ownBox, otherBox);

        vizBox.getChildren().addAll(titleLabel, legendBox, visualizationCanvas);

        splitPane.getItems().addAll(tableBox, vizBox);
        return splitPane;
    }

    private HBox createFilterPanel() {
        HBox filterBox = new HBox(10);

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("ID", lang.getString("table.name"), lang.getString("table.studentsCount"));
        filterCombo.setValue(lang.getString("table.name"));

        TextField filterField = new TextField();
        filterField.setPromptText(lang.getString("main.filter"));
        filterField.textProperty().addListener((obs, old, val) ->
                tableManager.applyFilter(filterCombo.getValue(), val));

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("ID", lang.getString("table.name"), lang.getString("table.studentsCount"));
        sortCombo.setValue("ID");
        sortCombo.setOnAction(e -> tableManager.applySort(sortCombo.getValue()));

        filterBox.getChildren().addAll(
                new Label(lang.getString("main.filter")), filterCombo, filterField,
                new Label(lang.getString("main.sort")), sortCombo
        );
        return filterBox;
    }

    private void refreshTable() {
        CommandResponse response = commandSender.send(CommandType.SHOW);
        if (response.isSuccess() && response.getCollection() != null) {
            tableManager.updateData(response.getCollection(), currentUserId);
            visualizationCanvas.updateObjects(response.getCollection(), currentUserId);
        } else {
            dialogManager.showError(response.getMessage());
            AnimationManager.shake(tableManager.getTableView());
        }
    }

    private void sendInfoCommand() {
        CommandResponse response = commandSender.send(CommandType.INFO);
        dialogManager.showInfo(response.getMessage());
    }

    private void sendClearCommand() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(lang.getString("dialog.clear.title"));
        confirm.setHeaderText(lang.getString("dialog.clear.header"));
        confirm.setContentText(lang.getString("dialog.clear.content"));
        AnimationManager.pulse(confirm.getDialogPane());

        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                commandSender.send(CommandType.CLEAR);
                refreshTable();
            }
        });
    }

    private void sendMinBySemesterCommand() {
        CommandResponse response = commandSender.send(CommandType.MIN_BY_SEMESTER_ENUM);
        if (response.isSuccess() && response.getGroup() != null) {
            showObjectInfo(response.getGroup());
        } else {
            dialogManager.showError(response.getMessage());
            AnimationManager.shake(tableManager.getTableView());
        }
    }

    private void showObjectInfo(StudyGroup group) {
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
        AnimationManager.fadeIn(info.getDialogPane(), Duration.millis(200));
        info.showAndWait();
    }

    private void changeLanguage() {
        String selected = languageSelector.getValue();
        if (selected.contains("English")) lang.setLocale("en_AU");
        else if (selected.contains("Nederlands")) lang.setLocale("nl");
        else if (selected.contains("Svenska")) lang.setLocale("sv");
        else lang.setLocale("ru");

        refreshUI();
        refreshTable();
    }

    private void refreshUI() {
        stage.setTitle(lang.getString("app.title") + " - " + username);
        userLabel.setText(lang.getString("main.user") + " " + username);

        if (refreshBtn != null) refreshBtn.setText(lang.getString("main.refresh"));
        if (addBtn != null) addBtn.setText(lang.getString("main.add"));
        if (editBtn != null) editBtn.setText(lang.getString("main.edit"));
        if (deleteBtn != null) deleteBtn.setText(lang.getString("main.delete"));
        if (infoBtn != null) infoBtn.setText(lang.getString("cmd.info"));
        if (clearBtn != null) clearBtn.setText(lang.getString("cmd.clear"));
        if (addIfMaxBtn != null) addIfMaxBtn.setText(lang.getString("cmd.add_if_max"));
        if (removeGreaterBtn != null) removeGreaterBtn.setText(lang.getString("cmd.remove_greater"));
        if (removeByStudentsBtn != null) removeByStudentsBtn.setText(lang.getString("cmd.remove_any_by_students_count"));
        if (minBySemesterBtn != null) minBySemesterBtn.setText(lang.getString("cmd.min_by_semester_enum"));
        if (countGreaterBtn != null) countGreaterBtn.setText(lang.getString("cmd.count_greater"));
        if (logoutBtn != null) logoutBtn.setText(lang.getString("main.logout"));

        tableManager.updateColumnTexts();

        updateLegendBox();

        updateFilterPanelTexts();
    }

    private HBox createLegendBox() {
        HBox legendBox = new HBox(20);
        legendBox.setAlignment(javafx.geometry.Pos.CENTER);

        HBox ownBox = new HBox(5);
        javafx.scene.shape.Circle greenCircle = new javafx.scene.shape.Circle(8, javafx.scene.paint.Color.GREEN);
        Label ownLabel = new Label(lang.getString("visualization.own"));
        ownLabel.setStyle("-fx-text-fill: white;");
        ownBox.getChildren().addAll(greenCircle, ownLabel);

        HBox otherBox = new HBox(5);
        javafx.scene.shape.Circle redCircle = new javafx.scene.shape.Circle(8, javafx.scene.paint.Color.RED);
        Label otherLabel = new Label(lang.getString("visualization.other"));
        otherLabel.setStyle("-fx-text-fill: white;");
        otherBox.getChildren().addAll(redCircle, otherLabel);

        legendBox.getChildren().addAll(ownBox, otherBox);
        return legendBox;
    }

    private void updateLegendBox() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane splitPane = (SplitPane) root.getCenter();
        VBox vizBox = (VBox) splitPane.getItems().get(1);

        if (vizBox.getChildren().size() > 1) {
            vizBox.getChildren().remove(1);
        }
        vizBox.getChildren().add(1, createLegendBox());
    }

    private void updateFilterPanelTexts() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane splitPane = (SplitPane) root.getCenter();
        VBox tableBox = (VBox) splitPane.getItems().get(0);
        tableBox.getChildren().set(0, createFilterPanel());
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> refreshTable()));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void logout() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
        commandSender.sendLogout();
        client.disconnect();
        AnimationManager.fadeOut(stage.getScene().getRoot(), Duration.millis(300), () -> {
            stage.close();
            new AuthController(new Stage(), client);
        });
    }
}