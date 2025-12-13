package org.eldir.client.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.eldir.client.controller.AppController;

public class LoginView {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private AppController appController;

    // Метод для внедрения главного контроллера
    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    private void onLoginClicked() {
        String login = loginField.getText();
        String pass = passwordField.getText();

        if (appController != null) {
            appController.handleLogin(login, pass);
        }
    }

    public void setError(String msg) {
        errorLabel.setText(msg);
    }
}