package org.eldir.client.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class HypercubeCanvas extends Canvas {

    // Геометрия гиперкуба (вершины и ребра)
    private final double[][] vertices = new double[16][4];
    private final int[][] edges = {
            {0,1},{0,2},{0,4},{0,8},{1,3},{1,5},{1,9},{2,3},{2,6},{2,10},{3,7},{3,11},{4,5},{4,6},
            {4,12},{5,7},{5,13},{6,7},{6,14},{7,15},{8,9},{8,10},{8,12},{9,11},{9,13},{10,11},
            {10,14},{11,15},{12,13},{12,14},{13,15},{14,15}
    };

    private double angleXY = 0, angleZW = 0, angleXW = 0;
    private double rotationSpeed = 0.02;
    private AnimationTimer timer;

    public HypercubeCanvas(double width, double height) {
        super(width, height);

        // Инициализация вершин гиперкуба
        int i = 0;
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                for (int z = -1; z <= 1; z += 2) {
                    for (int w = -1; w <= 1; w += 2) {
                        vertices[i++] = new double[]{x, y, z, w};
                    }
                }
            }
        }

        // Анимация
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                angleXY += rotationSpeed;
                angleZW += rotationSpeed * 0.5;
                angleXW += rotationSpeed * 1.5;
                draw();
            }
        };
        timer.start();
    }

    public void setRotationSpeed(double speed) {
        this.rotationSpeed = speed;
    }

    public void stopAnimation() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        // Очистка экрана
        gc.setFill(Color.rgb(40, 40, 40));
        gc.fillRect(0, 0, getWidth(), getHeight());

        // Центрируем
        gc.translate(getWidth() / 2, getHeight() / 2);

        // Проецируем все 16 вершин
        List<Point2D> projectedPoints = new ArrayList<>();
        for (double[] vertex : vertices) {
            double[] rotated = rotate(vertex);
            double w_perspective = 1 / (4 - rotated[3]);
            int scale = 200;
            projectedPoints.add(new Point2D(
                    rotated[0] * w_perspective * scale,
                    rotated[1] * w_perspective * scale
            ));
        }

        // Рисуем ребра
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(2);

        for (int[] edge : edges) {
            Point2D p1 = projectedPoints.get(edge[0]);
            Point2D p2 = projectedPoints.get(edge[1]);
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Рисуем вершины
        gc.setFill(Color.YELLOW);
        for (Point2D p : projectedPoints) {
            gc.fillOval(p.x - 3, p.y - 3, 6, 6);
        }

        // Возвращаем трансформацию
        gc.translate(-getWidth() / 2, -getHeight() / 2);
    }

    private double[] rotate(double[] p) {
        double[] r = p.clone();

        // Поворот в плоскости XY
        double x = r[0], y = r[1];
        r[0] = x * Math.cos(angleXY) - y * Math.sin(angleXY);
        r[1] = x * Math.sin(angleXY) + y * Math.cos(angleXY);

        // Поворот в плоскости ZW
        double z = r[2], w = r[3];
        r[2] = z * Math.cos(angleZW) - w * Math.sin(angleZW);
        r[3] = z * Math.sin(angleZW) + w * Math.cos(angleZW);

        // Поворот в плоскости XW
        x = r[0];
        w = r[3];
        r[0] = x * Math.cos(angleXW) - w * Math.sin(angleXW);
        r[3] = x * Math.sin(angleXW) + w * Math.cos(angleXW);

        return r;
    }

    // Вспомогательный класс для 2D точек
    private static class Point2D {
        double x, y;
        Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}