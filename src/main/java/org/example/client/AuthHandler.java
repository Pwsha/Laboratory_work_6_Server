package org.example.client;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;

public class AuthHandler {
    private final Client client;
    private final OutputRequest outputRequest;
    private String authToken = null;
    private String currentLogin = null;

    public AuthHandler(Client client, OutputRequest outputRequest) {
        this.client = client;
        this.outputRequest = outputRequest;
    }

    public boolean handleLogin(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length < 2) {
            System.out.println("Формат: login логин пароль");
            return false;
        }

        String login = parts[0];
        String password = parts[1];

        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.LOGIN)
                .login(login)
                .password(password)
                .build();

        if (!client.isConnected() && !client.connect()) {
            System.out.println("Нет подключения к серверу");
            return false;
        }

        CommandResponse response = client.sendRequest(request);
        outputRequest.printResponse(response);

        if (response.isSuccess() && response.getMessage().contains("Ваш токен:")) {
            String msg = response.getMessage();
            authToken = msg.substring(msg.indexOf("Ваш токен:") + 11).trim();
            currentLogin = login;
            System.out.println("Авторизован как: " + currentLogin);
            return true;
        }
        return false;
    }

    public void handleRegister(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length < 2) {
            System.out.println("Формат: register логин пароль");
            return;
        }

        String login = parts[0];
        String password = parts[1];

        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.REGISTER)
                .login(login)
                .password(password)
                .build();

        if (!client.isConnected() && !client.connect()) {
            System.out.println("Нет подключения к серверу");
            return;
        }

        CommandResponse response = client.sendRequest(request);
        outputRequest.printResponse(response);
    }

    public void handleLogout() {
        if (authToken == null) {
            System.out.println("Вы не авторизованы");
            return;
        }

        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.LOGOUT)
                .stringArg(authToken)
                .build();

        if (client.isConnected()) {
            CommandResponse response = client.sendRequest(request);
            outputRequest.printResponse(response);
        }

        authToken = null;
        currentLogin = null;
        System.out.println("Вы вышли из системы");
    }

    private void sendLogout() {
        if (authToken == null) return;

        CommandRequest request = new CommandRequest.Builder()
                .type(CommandType.LOGOUT)
                .stringArg(authToken)
                .build();

        if (client.isConnected()) {
            CommandResponse response = client.sendRequest(request);
            outputRequest.printResponse(response);
        }
    }

    public String getAuthToken() {
        return authToken;
    }

    public boolean isAuthenticated() {
        return authToken != null;
    }
}