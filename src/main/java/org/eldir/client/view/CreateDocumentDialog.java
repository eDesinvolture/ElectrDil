package org.eldir.client.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.eldir.shared.grpc.AccessLevel;
import org.eldir.shared.grpc.FileType;

import java.util.HashMap;
import java.util.Map;

public class CreateDocumentDialog extends Dialog<Map<String, Object>> {

    private TextField titleField;
    private ComboBox<FileType> fileTypeCombo;
    private ComboBox<AccessLevel> accessLevelCombo;
    private CheckBox hypercubeCheckbox;

    public CreateDocumentDialog() {
        setTitle("Создать документ");
        setHeaderText("Заполните параметры нового документа");

        ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        titleField = new TextField();
        titleField.setPromptText("Название документа");
        titleField.setPrefWidth(300);

        fileTypeCombo = new ComboBox<>(FXCollections.observableArrayList(
                FileType.FILE_TYPE_PDF,
                FileType.FILE_TYPE_TXT,
                FileType.FILE_TYPE_DOCX,
                FileType.FILE_TYPE_MD
        ));
        fileTypeCombo.setValue(FileType.FILE_TYPE_PDF);
        fileTypeCombo.setPrefWidth(300);

        accessLevelCombo = new ComboBox<>(FXCollections.observableArrayList(
                AccessLevel.ACCESS_PUBLIC,
                AccessLevel.ACCESS_DSP,
                AccessLevel.ACCESS_SECRET,
                AccessLevel.ACCESS_TOP_SECRET,
                AccessLevel.ACCESS_BURN_AFTER_READING
        ));
        accessLevelCombo.setValue(AccessLevel.ACCESS_PUBLIC);
        accessLevelCombo.setPrefWidth(300);

        hypercubeCheckbox = new CheckBox("Содержит гиперкуб");

        grid.add(new Label("Название:"), 0, 0);
        grid.add(titleField, 1, 0);

        grid.add(new Label("Формат файла:"), 0, 1);
        grid.add(fileTypeCombo, 1, 1);

        grid.add(new Label("Уровень доступа:"), 0, 2);
        grid.add(accessLevelCombo, 1, 2);

        grid.add(hypercubeCheckbox, 1, 3);

        getDialogPane().setContent(grid);

        javafx.scene.Node createButton = getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty());
        });

        javafx.application.Platform.runLater(() -> titleField.requestFocus());

        setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Map<String, Object> result = new HashMap<>();
                result.put("title", titleField.getText());
                result.put("fileType", fileTypeCombo.getValue());
                result.put("accessLevel", accessLevelCombo.getValue());
                result.put("hypercube", hypercubeCheckbox.isSelected());
                return result;
            }
            return null;
        });
    }
}