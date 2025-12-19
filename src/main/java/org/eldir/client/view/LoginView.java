package org.eldir.client.view;

import javafx.application.Platform;
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
        if (errorLabel != null) {
            Platform.runLater(() -> errorLabel.setText(msg));
        } else {
            System.err.println("LOGIN ERROR: " + msg);
        }
    }

    public void clearError() {
        if (errorLabel != null) {
            Platform.runLater(() -> errorLabel.setText(""));
        }
    }
}