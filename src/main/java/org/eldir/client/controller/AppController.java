package org.eldir.client.controller;

import org.eldir.client.model.DocumentModel;
import org.eldir.client.pattern.IView;
import org.eldir.client.service.GrpcClientService;
import org.eldir.shared.grpc.AccessLevel;
import org.eldir.shared.grpc.Document;
import org.eldir.shared.grpc.FileType;

import java.util.List;
import java.util.Map;

public class AppController {

    private final GrpcClientService service;
    private final IView view;
    private final DocumentModel model;

    public AppController(GrpcClientService service, IView view, DocumentModel model) {
        this.service = service;
        this.view = view;
        this.model = model;
    }

    public void start() {
        view.showLogin(this);
    }

    public void handleLogin(String login, String password) {
        try {
            service.login(login, password);
            // Если успешно - загружаем документы
            refreshDocuments();
            // И показываем главный экран
            view.showDocuments(model, this);
        } catch (Exception e) {
            view.showError("Ошибка входа: " + e.getMessage());
        }
    }

    public String getCurrentUserLogin() {
        return model.getCurrentUserLogin();
    }

    public void refreshDocuments() {
        try {
            List<Document> docs = service.getDocuments();
            model.setDocuments(docs);
            // Если мы уже на главном экране, view само обновится?
            // В данной реализации MainFrame каждый раз грузит FXML заново при вызове showDocuments,
            // поэтому либо мы вызываем showDocuments снова, либо (лучше) добавляем update метод во View.
            // Но пока просто перерисуем:
            view.showDocuments(model, this);
        } catch (Exception e) {
            view.showError("Ошибка загрузки: " + e.getMessage());
        }
    }

    public void handleCreate(FileType type, AccessLevel access, Map<String, String> attrs) {
        try {
            service.createDocument(type, access, attrs);
            refreshDocuments(); // Обновляем список после создания
        } catch (Exception e) {
            view.showError("Ошибка создания: " + e.getMessage());
        }
    }

    public void handleOpenDocument(String id) {
        try {
            // 1. Делаем запрос к серверу
            Document doc = service.getDocument(id);

            // 2. Показываем результат (например, просто текст атрибутов)
            StringBuilder info = new StringBuilder();
            info.append("ID: ").append(doc.getId()).append("\n");
            info.append("Type: ").append(doc.getFileType()).append("\n");
            info.append("Access: ").append(doc.getAccessLevel()).append("\n");
            info.append("Attributes: \n");
            doc.getAttributesMap().forEach((k, v) -> info.append(" - ").append(k).append(": ").append(v).append("\n"));

            String message = info.toString();

            // Если это был сжигаемый файл, добавим предупреждение
            if (doc.getAccessLevel() == AccessLevel.ACCESS_BURN_AFTER_READING) {
                message += "\n\n!!! ВНИМАНИЕ !!!\nЭтот документ был уничтожен на сервере после открытия.";
            }

            // Показываем Alert (в реальном приложении тут открылся бы PDF)
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Просмотр документа");
            alert.setHeaderText("Содержимое документа");
            alert.setContentText(message);
            alert.showAndWait();

            // 3. Обновляем список, чтобы увидеть, удалился он или нет
            refreshDocuments();

        } catch (Exception e) {
            view.showError("Не удалось открыть документ: " + e.getMessage());
        }
    }
}