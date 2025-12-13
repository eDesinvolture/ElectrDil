package org.eldir;

import javafx.application.Application;
import javafx.stage.Stage;
import org.eldir.client.controller.AppController;
import org.eldir.client.model.DocumentModel;
import org.eldir.client.service.GrpcClientService;
import org.eldir.client.view.MainFrame;

public class EldirClientApplication extends Application {

    private GrpcClientService service;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Создаем Модель (Хранит данные)
            DocumentModel model = new DocumentModel();

            // 2. Создаем Сервис (Отвечает за Сеть и SSL)
            // При создании он сразу попытается настроить SSL контекст
            service = new GrpcClientService();

            // 3. Создаем Вью (Отвечает за окна и FXML)
            MainFrame view = new MainFrame(primaryStage);

            // 4. Создаем Контроллер (Связывает всё воедино)
            AppController controller = new AppController(service, view, model);

            // 5. Запускаем приложение (покажет окно входа)
            controller.start();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Критическая ошибка при запуске клиента: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        // Этот метод вызывается, когда закрываешь окно
        System.out.println("Остановка клиента...");
        if (service != null) {
            service.shutdown(); // Закрываем сетевое соединение корректно
        }
        super.stop();
    }

    public static void main(String[] args) {
        // Стандартный запуск JavaFX
        launch(args);
    }
}