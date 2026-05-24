package org.eldir.client.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import org.eldir.shared.grpc.AccessLevel;
import org.eldir.shared.grpc.Document;
import org.eldir.shared.grpc.FileType;
import org.eldir.client.controller.AppController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainView {

    @FXML private TableView<Document> documentsTable;
    @FXML private TableColumn<Document, String> idColumn;
    @FXML private TableColumn<Document, String> typeColumn;
    @FXML private TableColumn<Document, String> accessColumn;
    @FXML private TableColumn<Document, String> titleColumn;
    @FXML private Button adminCreateUserBtn;
    @FXML private Label statusLabel;
    @FXML private Label userLabel;

    private AppController appController;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFileType().name()));
        accessColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccessLevel().name()));

        titleColumn.setCellValueFactory(data -> {
            String title = data.getValue().getAttributesMap().getOrDefault("title", "---");
            return new SimpleStringProperty(title);
        });

        // Двойной клик для открытия документа
        documentsTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Document> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Document rowData = row.getItem();
                    openDocumentDetails(rowData.getId());
                }
            });
            return row;
        });
    }

    private void openDocumentDetails(String id) {
        appController.handleOpenDocument(id);
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
        if (appController != null) {
            String login = appController.getCurrentUserLogin();
            userLabel.setText("Пользователь: " + login);

            if ("admin".equals(login)) {
                adminCreateUserBtn.setVisible(true);
                adminCreateUserBtn.setManaged(true);
            } else {
                adminCreateUserBtn.setVisible(false);
                adminCreateUserBtn.setManaged(false);
            }
        }
    }

    public void updateData(List<Document> documents) {
        documentsTable.setItems(FXCollections.observableArrayList(documents));
        statusLabel.setText("Загружено документов: " + documents.size());
    }

    @FXML
    private void onCreateDocument() {
        CreateDocumentDialog dialog = new CreateDocumentDialog();
        dialog.showAndWait().ifPresent(result -> {
            String title = (String) result.get("title");
            FileType fileType = (FileType) result.get("fileType");
            AccessLevel accessLevel = (AccessLevel) result.get("accessLevel");
            boolean hasHypercube = (Boolean) result.get("hypercube");

            Map<String, String> attrs = new HashMap<>();
            attrs.put("title", title);
            attrs.put("author_name", appController.getCurrentUserLogin());
            attrs.put("is_hypercube", String.valueOf(hasHypercube));

            appController.handleCreate(fileType, accessLevel, attrs);
        });
    }

    @FXML
    private void onCreateUser() {
        CreateUserDialog dialog = new CreateUserDialog();
        dialog.showAndWait().ifPresent(res -> {
            appController.handleCreateUser(
                    (String) res.get("login"),
                    (String) res.get("password"),
                    (AccessLevel) res.get("level")
            );
        });
    }

    @FXML
    private void onRefresh() {
        appController.refreshDocuments();
    }

    @FXML
    private void onLogout() {
        appController.handleLogout();
    }
}