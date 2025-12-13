package org.eldir.client.pattern;

public interface ISubject {
    void addObserver(IObserver observer);
    void notifyObservers();
}