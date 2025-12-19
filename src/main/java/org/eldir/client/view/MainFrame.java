package org.eldir.client.view;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.eldir.client.controller.AppController;
import org.eldir.client.model.DocumentModel;
import org.eldir.client.pattern.IView;

import java.io.IOException;

public class MainFrame implements IView {

    private final Stage stage;
    private LoginView activeLoginView;
    private MainView activeMainView;

    public MainFrame(Stage stage) {
        this.stage = stage;
        this.stage.setTitle("Eldir Client System");
    }

    @Override
    public void showLogin(AppController controller) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                Parent root = loader.load();

                activeLoginView = loader.getController();
                activeLoginView.setAppController(controller);
                activeLoginView.clearError();

                stage.setScene(new Scene(root, 400, 300));
                stage.centerOnScreen();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("ОШИБКА: Не найден файл /login.fxml");
            }
        });
    }

    @Override
    public void showDocuments(DocumentModel model, AppController controller) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
                Parent root = loader.load();

                activeMainView = loader.getController();
                activeMainView.setAppController(controller);
                activeMainView.updateData(model.getDocuments());

                stage.setScene(new Scene(root, 800, 600));
                stage.centerOnScreen();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("ОШИБКА: Не найден файл /main.fxml");
            }
        });
    }

    @Override
    public void showHypercube(String documentTitle) {
        Platform.runLater(() -> {
            HypercubeWindow hypercubeWindow = new HypercubeWindow(documentTitle);
            hypercubeWindow.show();
        });
    }

    @Override
    public void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация");
            alert.setHeaderText(null);
            alert.setContentText(message);

            alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> alert.close());

            alert.showAndWait();


        });
    }

    @Override
    public void showSucsess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Успешно");
            alert.setHeaderText(null);
            alert.setContentText(message);

            alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> alert.close());

            alert.showAndWait();
        });
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            if (activeLoginView != null && stage.getScene() != null &&
                    stage.getScene().getRoot().lookup("#loginField") != null) {
                activeLoginView.setError(message);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText(message);

                alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> alert.close());

                alert.showAndWait();
            }
        });
    }
}