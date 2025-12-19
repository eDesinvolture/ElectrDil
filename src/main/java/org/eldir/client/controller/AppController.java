package org.eldir.client.controller;

import org.eldir.client.model.DocumentModel;
import org.eldir.client.pattern.IView;
import org.eldir.client.service.GrpcClientService;
import org.eldir.client.view.HypercubeCanvas;
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
            model.setCurrentUserLogin(login);
            refreshDocuments();
            view.showDocuments(model, this);
        } catch (Exception e) {
            view.showError("Ошибка входа: " + e.getMessage());
        }
    }

    public void handleLogout() {
        try {
            service.logout();
            model.setDocuments(List.of());
            model.setCurrentUserLogin(null);
            view.showLogin(this);
        } catch (Exception e) {
            view.showError("Ошибка выхода: " + e.getMessage());
        }
    }

    public String getCurrentUserLogin() {
        return model.getCurrentUserLogin();
    }

    public void refreshDocuments() {
        try {
            List<Document> docs = service.getDocuments();
            model.setDocuments(docs);
            view.showDocuments(model, this);
        } catch (Exception e) {
            view.showError("Ошибка загрузки: " + e.getMessage());
        }
    }

    public void handleCreate(FileType type, AccessLevel access, Map<String, String> attrs) {
        try {
            service.createDocument(type, access, attrs);
            refreshDocuments();
        } catch (Exception e) {
            view.showError("Ошибка создания: " + e.getMessage());
        }
    }

    public void handleOpenDocument(String id) {
        try {
            Document doc = service.getDocument(id);

            boolean isBurnAfterReading = doc.getAccessLevel() == AccessLevel.ACCESS_BURN_AFTER_READING;
            String isHypercube = doc.getAttributesMap().get("is_hypercube");

            StringBuilder info = new StringBuilder();
            info.append("ID: ").append(doc.getId()).append("\n");
            info.append("Тип: ").append(doc.getFileType()).append("\n");
            info.append("Доступ: ").append(doc.getAccessLevel()).append("\n");
            info.append("Атрибуты:\n");
            doc.getAttributesMap().forEach((k, v) -> info.append(" - ").append(k).append(": ").append(v).append("\n"));

            String message = info.toString();
            if (isBurnAfterReading) {
                message += "\n\n!!! ДОКУМЕНТ УНИЧТОЖЕН ПОСЛЕ ПРОЧТЕНИЯ !!!";
            }

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Просмотр документа");
            alert.setHeaderText("Содержимое");
            alert.setContentText(message);

            HypercubeCanvas hypercubeCanvas;
            if ("true".equalsIgnoreCase(isHypercube)) {
                hypercubeCanvas = new HypercubeCanvas(600, 400);

                javafx.scene.control.Slider speedSlider = new javafx.scene.control.Slider(0, 100, 20);
                speedSlider.setShowTickLabels(true);
                speedSlider.setShowTickMarks(true);
                speedSlider.setMajorTickUnit(25);

                javafx.scene.control.Label speedLabel = new javafx.scene.control.Label("Скорость вращения: 20");

                HypercubeCanvas finalCanvas = hypercubeCanvas;
                speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    finalCanvas.setRotationSpeed(newVal.doubleValue() / 1000.0);
                    speedLabel.setText(String.format("Скорость: %.0f", newVal.doubleValue()));
                });

                hypercubeCanvas.setRotationSpeed(speedSlider.getValue() / 1000.0);

                javafx.scene.layout.VBox hypercubeBox = new javafx.scene.layout.VBox(10, hypercubeCanvas, speedLabel, speedSlider);
                hypercubeBox.setPadding(new javafx.geometry.Insets(10));

                alert.getDialogPane().setExpandableContent(hypercubeBox);
                alert.getDialogPane().setExpanded(true);
            } else {
                hypercubeCanvas = null;
            }

            alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> {
                if (hypercubeCanvas != null) {
                    hypercubeCanvas.stopAnimation();
                }
                alert.close();
            });

            javafx.scene.control.ButtonType deleteBtn = new javafx.scene.control.ButtonType("Удалить", javafx.scene.control.ButtonBar.ButtonData.OTHER);
            javafx.scene.control.ButtonType okBtn = new javafx.scene.control.ButtonType("OK", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);

            if (!isBurnAfterReading) {
                alert.getButtonTypes().setAll(deleteBtn, okBtn);
            } else {
                alert.getButtonTypes().setAll(okBtn);
            }

            HypercubeCanvas finalCanvas2 = hypercubeCanvas;
            var result = alert.showAndWait();

            if (finalCanvas2 != null) {
                finalCanvas2.stopAnimation();
            }

            if (result.isPresent()) {
                if (result.get() == deleteBtn) {
                    try {
                        service.deleteDocument(id);
                        view.showInfo("Документ удален.");
                        refreshDocuments();
                    } catch (Exception ex) {
                        view.showError("Ошибка удаления: " + ex.getMessage());
                    }
                } else if (result.get() == okBtn && isBurnAfterReading) {
                    refreshDocuments();
                }
            }

        } catch (Exception e) {
            view.showError("Не удалось открыть документ: " + e.getMessage());
        }
    }

    public void handleCreateUser(String login, String password, AccessLevel level) {
        try {
            service.createUser(login, password, level);
            view.showSucsess("Пользователь " + login + " успешно создан!");
        } catch (Exception e) {
            view.showError("Ошибка создания пользователя: " + e.getMessage());
        }
    }
}