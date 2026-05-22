package org.example.client.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.client.Client;
import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;

public class AuthController {
    private final Stage stage;
    private final Client client;
    private final LanguageManager lang;
    private MainController mainController;
    private TextField loginField;
    private PasswordField passwordField;
    private ComboBox<String> languageSelector;
    private Label errorLabel;

    public AuthController(Stage stage, Client client) {
        this.stage = stage;
        this.client = client;
        this.lang = LanguageManager.getInstance();
        initUI();
    }

    private void initUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2b3b4c, #1a2a3a);");

        Label titleLabel = new Label(lang.getString("app.title"));
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox langBox = new HBox(10);
        langBox.setAlignment(Pos.CENTER_RIGHT);
        languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll("Русский", "English (AU)", "Nederlands", "Svenska");
        languageSelector.setValue("Русский");
        languageSelector.setOnAction(e -> changeLanguage());
        langBox.getChildren().addAll(new Label("Language:"), languageSelector);

        VBox formBox = new VBox(15);
        formBox.setMaxWidth(300);
        formBox.setAlignment(Pos.CENTER);
        formBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");
        formBox.setPadding(new Insets(20));

        loginField = new TextField();
        loginField.setPromptText(lang.getString("auth.login"));
        passwordField = new PasswordField();
        passwordField.setPromptText(lang.getString("auth.password"));
        Button loginButton = new Button(lang.getString("auth.signin"));
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        loginButton.setOnAction(e -> handleLogin());
        Button registerButton = new Button(lang.getString("auth.register"));
        registerButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        registerButton.setOnAction(e -> handleRegister());
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #f44336;");
        errorLabel.setVisible(false);

        formBox.getChildren().addAll(loginField, passwordField, loginButton, registerButton, errorLabel);
        root.getChildren().addAll(titleLabel, langBox, formBox);

        stage.setScene(new Scene(root, 500, 500));
        stage.setTitle(lang.getString("app.title"));
        stage.show();
    }

    private void changeLanguage() {
        String selected = languageSelector.getValue();
        if (selected.contains("English")) lang.setLocale("en_AU");
        else if (selected.contains("Nederlands")) lang.setLocale("nl");
        else if (selected.contains("Svenska")) lang.setLocale("sv");
        else lang.setLocale("ru");

        loginField.setPromptText(lang.getString("auth.login"));
        passwordField.setPromptText(lang.getString("auth.password"));
    }

    private void handleLogin() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText(lang.getString("auth.error.empty"));
            errorLabel.setVisible(true);
            return;
        }

        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.LOGIN)
                .login(login)
                .password(password)
                .build();

        if (!client.isConnected() && !client.connect()) {
            errorLabel.setText(lang.getString("error.connection"));
            errorLabel.setVisible(true);
            return;
        }


        CommandResponse response = client.sendRequest(request);

        if (response.isSuccess()) {
            String token = response.getMessage().contains("Ваш токен:")
                    ? response.getMessage().substring(response.getMessage().indexOf("Ваш токен:") + 11).trim()
                    : null;
            if (token != null) {
                Integer userId = response.getUserId();
                if (userId == null) userId = 1;

                if (mainController != null) {
                    mainController.switchUser(token, login, userId);
                    stage.setTitle(lang.getString("app.title") + " - " + login);
                } else {
                    mainController = new MainController(stage, client, token, login, userId);
                    mainController.show();
                }
            }
        } else {
            errorLabel.setText(lang.getString("auth.error.invalid"));
            errorLabel.setVisible(true);
        }
    }

    private void handleRegister() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText(lang.getString("auth.error.empty"));
            errorLabel.setVisible(true);
            return;
        }

        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.REGISTER)
                .login(login)
                .password(password)
                .build();

        if (!client.isConnected() && !client.connect()) {
            errorLabel.setText(lang.getString("error.connection"));
            errorLabel.setVisible(true);
            return;
        }

        CommandResponse response = client.sendRequest(request);

        if (response.isSuccess()) {
            errorLabel.setStyle("-fx-text-fill: #4CAF50;");
            errorLabel.setText(response.getMessage());
            errorLabel.setVisible(true);
        } else {
            errorLabel.setText(response.getMessage());
            errorLabel.setVisible(true);
        }
    }
}