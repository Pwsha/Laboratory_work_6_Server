package org.example.client.gui.controller;

import org.example.client.Client;
import org.example.common.command.CommandRequest;
import org.example.common.command.CommandResponse;
import org.example.common.command.CommandType;
import org.example.common.init.StudyGroup;

public class CommandSender {
    private final Client client;
    private String authToken;

    public CommandSender(Client client, String authToken) {
        this.client = client;
        this.authToken = authToken;
    }

    public void updateAuthToken(String newAuthToken) {
        this.authToken = newAuthToken;
    }

    private CommandRequest.Builder baseRequest(CommandType type) {
        return new CommandRequest.Builder().type(type).stringArg(authToken);
    }

    private CommandResponse sendRequest(CommandRequest request) {
        return client.sendRequest(request);
    }

    public CommandResponse send(CommandType type) {
        return sendRequest(baseRequest(type).build());
    }

    public CommandResponse send(CommandType type, StudyGroup group) {
        return sendRequest(baseRequest(type).studyGroup(group).build());
    }

    public CommandResponse send(CommandType type, Long id) {
        return sendRequest(baseRequest(type).id(id).build());
    }

    public CommandResponse send(CommandType type, Long id, StudyGroup group) {
        return sendRequest(baseRequest(type).id(id).studyGroup(group).build());
    }

    public CommandResponse send(CommandType type, Integer value) {
        return sendRequest(baseRequest(type).expelledStudents(value).build());
    }

    public CommandResponse sendUpdate(Long id, StudyGroup group) {
        return send(CommandType.UPDATE, id, group);
    }

    public CommandResponse sendRemoveById(Long id) {
        return send(CommandType.REMOVE_BY_ID, id);
    }

    public CommandResponse sendLogout() {
        return send(CommandType.LOGOUT);
    }
}