package org.eldir.client.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.eldir.shared.grpc.AccessLevel;

import java.util.HashMap;
import java.util.Map;

public class CreateUserDialog extends Dialog<Map<String, Object>> {

    public CreateUserDialog() {
        setTitle("Регистрация пользователя");
        setHeaderText("Создание нового сотрудника");

        ButtonType createType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField login = new TextField();
        login.setPromptText("Login");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        ComboBox<AccessLevel> levelCombo = new ComboBox<>(FXCollections.observableArrayList(
                AccessLevel.ACCESS_PUBLIC,
                AccessLevel.ACCESS_DSP,
                AccessLevel.ACCESS_SECRET,
                AccessLevel.ACCESS_TOP_SECRET
        ));
        levelCombo.setValue(AccessLevel.ACCESS_PUBLIC);

        grid.add(new Label("Логин:"), 0, 0);
        grid.add(login, 1, 0);
        grid.add(new Label("Пароль:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Допуск:"), 0, 2);
        grid.add(levelCombo, 1, 2);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == createType) {
                Map<String, Object> map = new HashMap<>();
                map.put("login", login.getText());
                map.put("password", password.getText());
                map.put("level", levelCombo.getValue());
                return map;
            }
            return null;
        });
    }
}