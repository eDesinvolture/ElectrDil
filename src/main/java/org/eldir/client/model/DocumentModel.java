package org.eldir.client.model;

import org.eldir.client.pattern.IObserver;
import org.eldir.client.pattern.ISubject;
import org.eldir.shared.grpc.Document;

import java.util.ArrayList;
import java.util.List;


public class DocumentModel implements ISubject {
    private final List<IObserver> observers = new ArrayList<>();
    private List<Document> documents = new ArrayList<>();
    private Document selectedDocument;
    private String currentUserLogin;

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
        notifyObservers();
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setSelectedDocument(Document document) {
        this.selectedDocument = document;
    }

    public String getCurrentUserLogin() {
        return currentUserLogin;
    }

    public void setCurrentUserLogin(String login) {
        this.currentUserLogin = login;
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