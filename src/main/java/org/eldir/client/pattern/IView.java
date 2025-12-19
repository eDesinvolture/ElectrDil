package org.eldir.client.pattern;

import org.eldir.client.controller.AppController;
import org.eldir.client.model.DocumentModel;

public interface IView {
    void showLogin(AppController controller);
    void showDocuments(DocumentModel model, AppController controller);
    void showError(String message);
    void showInfo(String message);
    void showSucsess(String message);
    void showHypercube(String documentTitle);
}