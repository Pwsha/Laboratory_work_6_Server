package org.example.server;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.server.data.DataManager;

import java.util.Optional;

public class AuthManager {
    private final DataManager dbManager;
    private final SessionManager sessionManager;

    public AuthManager(DataManager dbManager, SessionManager sessionManager) {
        this.dbManager = dbManager;
        this.sessionManager = sessionManager;
    }

    public CommandResponse handleLogin(CommandRequest request) {
        String login = request.getLogin();
        String password = request.getPassword();

        if (login == null || password == null || login.isEmpty() || password.isEmpty()) {
            return CommandResponse.error("Логин и пароль обязательны");
        }

        String hashedPassword = PasswordHasher.hash(password);
        Optional<Integer> userIdOpt = dbManager.authenticate(login, hashedPassword);

        if (userIdOpt.isPresent()) {
            int userId = userIdOpt.get();
            String token = sessionManager.createSession(userId);

            return CommandResponse.builder()
                    .success(true)
                    .message("Успешный вход. Ваш токен: " + token)
                    .userId(userId)
                    .build();
        }

        return CommandResponse.error("Неверный логин или пароль");
    }

    public CommandResponse handleRegister(CommandRequest request) {
        String login = request.getLogin();
        String password = request.getPassword();

        if (login == null || password == null || login.isEmpty() || password.isEmpty()) {
            return CommandResponse.error("Логин и пароль обязательны");
        }

        String hashedPassword = PasswordHasher.hash(password);
        boolean success = dbManager.registerUser(login, hashedPassword);

        if (success) {
            return CommandResponse.success("Регистрация успешна. Теперь войдите (login)");
        }

        return CommandResponse.error("Пользователь с таким логином уже существует");
    }

    public CommandResponse handleLogout(CommandRequest request) {
        String token = request.getStringArg();
        if (token != null && !token.isEmpty()) {
            sessionManager.removeSession(token);
        }
        return CommandResponse.success("Выход выполнен");
    }

    public boolean isAuthorized(CommandRequest request) {
        CommandType type = request.getType();

        if (type == CommandType.LOGIN || type == CommandType.REGISTER || type == CommandType.LOGOUT) {
            return true;
        }

        String token = request.getStringArg();
        return sessionManager.isAuthenticated(token);
    }

    public Optional<Integer> getUserId(CommandRequest request) {
        String token = request.getStringArg();
        return sessionManager.getUserId(token);
    }

    public Optional<Integer> getUserIdFromToken(String token) {
        return sessionManager.getUserId(token);
    }
}