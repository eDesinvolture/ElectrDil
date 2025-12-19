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
            DocumentModel model = new DocumentModel();
            service = new GrpcClientService();

            MainFrame view = new MainFrame(primaryStage);
            AppController controller = new AppController(service, view, model);
            controller.start();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Критическая ошибка при запуске клиента: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        if (service != null) {
            service.shutdown();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}