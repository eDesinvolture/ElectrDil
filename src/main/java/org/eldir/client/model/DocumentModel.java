package org.eldir.client.model;

import lombok.Getter;
import lombok.Setter;
import org.eldir.client.pattern.IObserver;
import org.eldir.client.pattern.ISubject;
import org.eldir.shared.grpc.Document;

import java.util.ArrayList;
import java.util.List;


//@Setter
//@Getter
public class DocumentModel implements ISubject {
    private final List<IObserver> observers = new ArrayList<>();
    private List<Document> documents = new ArrayList<>();
    private Document selectedDocument;

    // Данные для создания нового
    private String currentUserLogin = "admin"; // Пока хардкод, позже из Security

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
        notifyObservers();
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setSelectedDocument(Document document) {
        this.selectedDocument = document;
        // Можно уведомить, если нужно обновить детали
    }

    public String getCurrentUserLogin() {
        return currentUserLogin;
    }

    @Override
    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers() {
        for (IObserver observer : observers) {
            observer.update();
        }
    }
}