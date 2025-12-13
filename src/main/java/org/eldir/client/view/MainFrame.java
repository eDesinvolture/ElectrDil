package org.eldir.client.view;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.eldir.client.controller.AppController;
import org.eldir.client.model.DocumentModel;
import org.eldir.client.pattern.IView;

import java.io.IOException;

public class MainFrame implements IView {

    private final Stage stage;

    // Ссылки на контроллеры экранов (LoginView и MainView)
    private LoginView activeLoginView;
    private MainView activeMainView;

    public MainFrame(Stage stage) {
        this.stage = stage;
        this.stage.setTitle("Eldir Client System");
    }

    @Override
    public void showLogin(AppController controller) {
        try {
            // Загружаем файл из resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            // Получаем Java-контроллер экрана входа и передаем ему главный контроллер
            activeLoginView = loader.getController();
            activeLoginView.setAppController(controller);

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ОШИБКА: Не найден файл /login.fxml в папке src/main/resources!");
        }
    }

    @Override
    public void showDocuments(DocumentModel model, AppController controller) {
        try {
            // Загружаем файл из resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();

            // Настраиваем контроллер главного экрана
            activeMainView = loader.getController();
            activeMainView.setAppController(controller);
            activeMainView.updateData(model.getDocuments());

            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ОШИБКА: Не найден файл /main.fxml в папке src/main/resources!");
        }
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            if (activeLoginView != null) {
                activeLoginView.setError(message);
            } else {
                System.err.println("ERROR: " + message);
            }
        });
    }
}