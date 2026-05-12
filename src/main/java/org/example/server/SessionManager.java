package org.example.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Управление сессиями пользователей.
 * При успешной авторизации выдаётся токен, который клиент использует для следующих запросов.
 */
public class SessionManager {
    private final Map<String, Integer> sessions = new ConcurrentHashMap<>();  // token -> userId
    private final Map<Integer, String> userTokens = new ConcurrentHashMap<>(); // userId -> token

    /**
     * Создание новой сессии для пользователя
     * @return token для клиента
     */
    public String createSession(int userId) {
        // Если уже есть сессия, удаляем старую
        if (userTokens.containsKey(userId)) {
            String oldToken = userTokens.remove(userId);
            sessions.remove(oldToken);
        }

        String token = UUID.randomUUID().toString();
        sessions.put(token, userId);
        userTokens.put(userId, token);
        return token;
    }

    /**
     * Получение userId по токену
     * @return Optional с userId, если сессия активна
     */
    public java.util.Optional<Integer> getUserId(String token) {
        if (token == null || token.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(sessions.get(token));
    }

    /**
     * Завершение сессии (выход)
     */
    public void removeSession(String token) {
        Integer userId = sessions.remove(token);
        if (userId != null) {
            userTokens.remove(userId);
        }
    }

    /**
     * Проверка, авторизован ли пользователь
     */
    public boolean isAuthenticated(String token) {
        return token != null && sessions.containsKey(token);
    }
}