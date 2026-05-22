package org.example.client.gui;

import javafx.animation.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.util.Duration;
import org.example.common.init.StudyGroup;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class VisualizationCanvas extends Canvas {
    private final Map<Long, VisualObject> visualObjects = new ConcurrentHashMap<>();
    private Consumer<StudyGroup> onObjectClick;
    private Consumer<StudyGroup> onObjectDoubleClick;

    private double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
    private double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
    private double padding = 50; // отступы от краёв

    public VisualizationCanvas(double width, double height) {
        super(width, height);
        setOnMouseClicked(this::handleMouseClick);
        widthProperty().addListener((obs, old, newVal) -> redraw());
        heightProperty().addListener((obs, old, newVal) -> redraw());
    }

    public void updateColors(int currentUserId) {
        for (VisualObject vo : visualObjects.values()) {
            Integer groupUserId = vo.group.getUserId();
            if (groupUserId != null && groupUserId == currentUserId) {
                vo.color = Color.GREEN;
            } else {
                vo.color = Color.RED;
            }
        }
        redraw();
    }

    public void updateObjects(List<StudyGroup> groups, int currentUserId) {
        calculateBounds(groups);

        Set<Long> currentIds = new HashSet<>();
        for (StudyGroup group : groups) {
            if (group == null || group.getId() == null) continue;
            currentIds.add(group.getId());
            VisualObject vo = visualObjects.get(group.getId());
            if (vo == null) {
                Color color = (group.getUserId() != null && group.getUserId() == currentUserId) ? Color.RED : Color.GREEN;
                vo = VisualObject.builder()
                        .id(group.getId())
                        .x(group.getCoordinates().getX())
                        .y(group.getCoordinates().getY())
                        .studentsCount(group.getStudentsCount())
                        .color(color)
                        .group(group)
                        .build();
                visualObjects.put(group.getId(), vo);
                vo.playAppearAnimation(this);
            } else {
                vo.update(group, currentUserId);
            }
        }

        List<Long> toRemove = new ArrayList<>();
        for (Long id : visualObjects.keySet()) {
            if (!currentIds.contains(id)) {
                VisualObject vo = visualObjects.get(id);
                vo.playDisappearAnimation(this, () -> visualObjects.remove(id));
                toRemove.add(id);
            }
        }

        redraw();
    }

    private void calculateBounds(List<StudyGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            // Значения по умолчанию, если нет объектов
            minX = 0;
            maxX = 1000;
            minY = 0;
            maxY = 800;
            return;
        }

        minX = Double.MAX_VALUE;
        maxX = -Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (StudyGroup group : groups) {
            if (group == null) continue;
            double x = group.getCoordinates().getX();
            double y = group.getCoordinates().getY();
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        double xRange = maxX - minX;
        double yRange = maxY - minY;

        if (xRange < 100) {
            minX -= 50;
            maxX += 50;
        } else {
            minX -= xRange * 0.1;
            maxX += xRange * 0.1;
        }

        if (yRange < 100) {
            minY -= 50;
            maxY += 50;
        } else {
            minY -= yRange * 0.1;
            maxY += yRange * 0.1;
        }
    }

    private void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        drawGrid(gc);

        for (VisualObject vo : visualObjects.values()) {
            vo.draw(gc, minX, maxX, minY, maxY, getWidth(), getHeight());
        }
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        double stepX = (maxX - minX) / 10;
        for (int i = 0; i <= 10; i++) {
            double x = minX + i * stepX;
            double canvasX = (x - minX) / (maxX - minX) * getWidth();
            gc.strokeLine(canvasX, 0, canvasX, getHeight());

            gc.setFill(Color.GRAY);
            gc.fillText(String.format("%.0f", x), canvasX - 15, getHeight() - 10);
        }

        double stepY = (maxY - minY) / 10;
        for (int i = 0; i <= 10; i++) {
            double y = minY + i * stepY;
            double canvasY = getHeight() - (y - minY) / (maxY - minY) * getHeight();
            gc.strokeLine(0, canvasY, getWidth(), canvasY);

            gc.setFill(Color.GRAY);
            gc.fillText(String.format("%.0f", y), 5, canvasY - 5);
        }
    }

    private void handleMouseClick(MouseEvent e) {
        double canvasX = e.getX();
        double canvasY = e.getY();

        for (VisualObject vo : visualObjects.values()) {
            double objX = (vo.x - minX) / (maxX - minX) * getWidth();
            double objY = getHeight() - (vo.y - minY) / (maxY - minY) * getHeight();
            double size = Math.max(20, Math.min(50, vo.studentsCount / 10.0));

            if (Math.hypot(canvasX - objX, canvasY - objY) < size) {
                if (e.getClickCount() == 2 && onObjectDoubleClick != null) {
                    onObjectDoubleClick.accept(vo.group);
                } else if (onObjectClick != null) {
                    onObjectClick.accept(vo.group);
                }
                break;
            }
        }
    }

    public void setOnObjectClick(Consumer<StudyGroup> callback) {
        this.onObjectClick = callback;
    }

    public void setOnObjectDoubleClick(Consumer<StudyGroup> callback) {
        this.onObjectDoubleClick = callback;
    }

    private static class VisualObject {
        private final Long id;
        private double x;
        private double y;
        private double studentsCount;
        private Color color;
        private StudyGroup group;
        private double scale = 1.0;
        private double opacity = 1.0;

        private VisualObject(Builder builder) {
            this.id = builder.id;
            this.x = builder.x;
            this.y = builder.y;
            this.studentsCount = builder.studentsCount;
            this.color = builder.color;
            this.group = builder.group;
        }

        void update(StudyGroup group, int currentUserId) {
            this.group = group;
            this.x = group.getCoordinates().getX();
            this.y = group.getCoordinates().getY();
            this.studentsCount = group.getStudentsCount();
            this.color = (group.getUserId() != null && group.getUserId() == currentUserId) ? Color.GREEN : Color.RED;
        }

        void draw(GraphicsContext gc, double minX, double maxX, double minY, double maxY, double width, double height) {
            double canvasX = (x - minX) / (maxX - minX) * width;
            double canvasY = height - (y - minY) / (maxY - minY) * height;
            double size = Math.max(15, Math.min(40, studentsCount / 15.0)) * scale;

            gc.save();
            gc.setGlobalAlpha(opacity);

            Stop[] stops = { new Stop(0, color), new Stop(1, color.darker()) };
            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
            gc.setFill(gradient);
            gc.fillOval(canvasX - size, canvasY - size, size * 2, size * 2);

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeOval(canvasX - size, canvasY - size, size * 2, size * 2);

            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", Math.max(10, size / 3)));
            gc.fillText(String.valueOf(id), canvasX - size / 3, canvasY + 5);

            gc.restore();
        }

        void playAppearAnimation(VisualizationCanvas canvas) {
            scale = 0;
            opacity = 0;
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> { scale = 0; opacity = 0; canvas.redraw(); }),
                    new KeyFrame(Duration.millis(500), e -> { scale = 1; opacity = 1; canvas.redraw(); })
            );
            timeline.play();
        }

        void playDisappearAnimation(VisualizationCanvas canvas, Runnable onFinish) {
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> { scale = 1; opacity = 1; canvas.redraw(); }),
                    new KeyFrame(Duration.millis(500), e -> { scale = 0; opacity = 0; canvas.redraw(); })
            );
            timeline.setOnFinished(e -> onFinish.run());
            timeline.play();
        }

        static Builder builder() { return new Builder(); }

        static class Builder {
            private Long id; private double x; private double y; private double studentsCount; private Color color; private StudyGroup group;
            Builder id(Long id) { this.id = id; return this; }
            Builder x(double x) { this.x = x; return this; }
            Builder y(double y) { this.y = y; return this; }
            Builder studentsCount(double studentsCount) { this.studentsCount = studentsCount; return this; }
            Builder color(Color color) { this.color = color; return this; }
            Builder group(StudyGroup group) { this.group = group; return this; }
            VisualObject build() { return new VisualObject(this); }
        }
    }
}