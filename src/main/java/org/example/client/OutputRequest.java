package org.example.client;

import org.example.common.command.CommandResponse;

public class OutputRequest {

    public void printResponse(CommandResponse response) {
        if (!response.isSuccess()) {
            System.out.println("Ошибка: " + response.getMessage());
            return;
        }

        System.out.println(response.getMessage());

        if (response.getCollection() != null && !response.getCollection().isEmpty()) {
            response.getCollection().forEach(g -> System.out.println("  " + g));
        }
        if (response.getGroup() != null) {
            System.out.println("  " + response.getGroup());
        }
        if (response.getCount() != null) {
            System.out.println("  " + response.getCount());
        }
    }
}