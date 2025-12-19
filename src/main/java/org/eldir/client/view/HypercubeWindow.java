package org.eldir.client.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HypercubeWindow {

    private final Stage stage;
    private final HypercubeCanvas canvas;

    public HypercubeWindow(String documentTitle) {
        stage = new Stage();
        stage.setTitle("Гиперкуб: " + documentTitle);

        canvas = new HypercubeCanvas(800, 600);

        Slider speedSlider = new Slider(0, 100, 20);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(25);
        speedSlider.setBlockIncrement(10);

        Label speedLabel = new Label("Скорость вращения: 20");

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double speed = newVal.doubleValue() / 1000.0;
            canvas.setRotationSpeed(speed);
            speedLabel.setText(String.format("Скорость вращения: %.0f", newVal.doubleValue()));
        });

        canvas.setRotationSpeed(speedSlider.getValue() / 1000.0);

        VBox controls = new VBox(10, speedLabel, speedSlider);
        controls.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(controls);

        Scene scene = new Scene(root, 800, 650);
        stage.setScene(scene);

        stage.setOnCloseRequest(e -> canvas.stopAnimation());
    }

    public void show() {
        stage.show();
    }
}