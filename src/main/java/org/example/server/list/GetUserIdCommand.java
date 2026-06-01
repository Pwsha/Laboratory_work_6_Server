package org.example.server.list;

import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.server.CollectionManager;
import org.example.server.AuthManager;

import java.util.Optional;
import java.util.Scanner;

public class GetUserIdCommand implements Command {
    private final AuthManager authManager;

    public GetUserIdCommand(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public CommandResponse execute(CommandRequest request, CollectionManager manager, Scanner scanner, int userId) {
        String token = request.getStringArg();
        Optional<Integer> userIdOpt = authManager.getUserIdFromToken(token);

        if (userIdOpt.isPresent()) {
            return CommandResponse.builder()
                    .success(true)
                    .userId(userIdOpt.get())
                    .message("OK")
                    .build();
        }

        return CommandResponse.error("Пользователь не найден");
    }

    @Override
    public String getName() { return "get_user_id"; }
    @Override
    public String getDescription() { return "получить ID пользователя по токену"; }
    @Override
    public String getSyntax() { return "get_user_id"; }
}