package org.eldir.client.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.eldir.client.controller.AppController;
import org.eldir.shared.grpc.AccessLevel;
import org.eldir.shared.grpc.Document;
import org.eldir.shared.grpc.FileType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainView {

    @FXML private TableView<Document> documentsTable;
    @FXML private TableColumn<Document, String> idColumn;
    @FXML private TableColumn<Document, String> typeColumn;
    @FXML private TableColumn<Document, String> accessColumn;
    @FXML private TableColumn<Document, String> titleColumn;
    @FXML private Label statusLabel;

    private AppController appController;

    @FXML
    public void initialize() {
        // Настраиваем, как отображать данные в столбцах
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFileType().name()));
        accessColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccessLevel().name()));

        // EAV атрибут "title"
        titleColumn.setCellValueFactory(data -> {
            String title = data.getValue().getAttributesMap().getOrDefault("title", "---");
            return new SimpleStringProperty(title);
        });
        documentsTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Document> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Document rowData = row.getItem();
                    // Вызываем открытие документа
                    openDocumentDetails(rowData.getId());
                }
            });
            return row ;
        });
    }
    private void openDocumentDetails(String id) {
        // Просим контроллер получить полные данные
        // (Это вызовет gRPC GetDocument, который активирует логику сжигания на сервере)
        appController.handleOpenDocument(id);
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void updateData(List<Document> documents) {
        documentsTable.setItems(FXCollections.observableArrayList(documents));
        statusLabel.setText("Загружено документов: " + documents.size());
    }

    @FXML
    private void onCreateSimple() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("title", "Секретный отчет №" + System.currentTimeMillis());
        attrs.put("author_name", appController.getCurrentUserLogin());

        appController.handleCreate(FileType.FILE_TYPE_PDF, AccessLevel.ACCESS_SECRET, attrs);
    }

    @FXML
    private void onCreateBurn() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("title", "Самоуничтожающееся послание");

        appController.handleCreate(FileType.FILE_TYPE_TXT, AccessLevel.ACCESS_BURN_AFTER_READING, attrs);
    }

    @FXML
    private void onRefresh() {
        appController.refreshDocuments();
    }
}