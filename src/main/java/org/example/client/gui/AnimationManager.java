package org.example.client.gui;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationManager {

    private AnimationManager() {}

    public static void fadeIn(Node node, Duration duration) {
        if (node == null) return;
        node.setOpacity(0);
        node.setVisible(true);
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    public static void fadeOut(Node node, Duration duration, Runnable onFinish) {
        if (node == null) {
            if (onFinish != null) onFinish.run();
            return;
        }
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            node.setVisible(false);
            if (onFinish != null) onFinish.run();
        });
        ft.play();
    }

    public static void shake(Node node) {
        if (node == null) return;
        TranslateTransition tt = new TranslateTransition(Duration.millis(100), node);
        tt.setFromX(0);
        tt.setToX(10);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }

    public static void pulse(Node node) {
        if (node == null) return;
        ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
        st.setFromX(1);
        st.setToX(1.05);
        st.setFromY(1);
        st.setToY(1.05);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }

    public static void bounce(Node node) {
        if (node == null) return;
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), node);
        tt.setFromY(0);
        tt.setToY(-10);
        tt.setCycleCount(2);
        tt.setAutoReverse(true);
        tt.play();
    }
}