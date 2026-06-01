package org.example.server;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, Integer> sessions = new ConcurrentHashMap<>();
    private final Map<Integer, String> userTokens = new ConcurrentHashMap<>();

    public String createSession(int userId) {
        if (userTokens.containsKey(userId)) {
            String oldToken = userTokens.remove(userId);
            sessions.remove(oldToken);
        }

        String token = UUID.randomUUID().toString();
        sessions.put(token, userId);
        userTokens.put(userId, token);
        return token;
    }

    public java.util.Optional<Integer> getUserId(String token) {
        if (token == null || token.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(sessions.get(token));
    }

    public void removeSession(String token) {
        Integer userId = sessions.remove(token);
        if (userId != null) {
            userTokens.remove(userId);
        }
    }

    public Optional<Integer> getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(token));
    }

    public boolean isAuthenticated(String token) {
        return token != null && sessions.containsKey(token);
    }
}