package org.itc.data;

import java.util.ArrayList;

import org.itc.scenario.IObserver;

public abstract class IStorageManager
{
    private ArrayList<IObserver> observers = new ArrayList<IObserver>();

    public IStorageManager()
    {}

    public abstract void initConnection();

    public abstract void closeConnection();

    public abstract double getMeasurementDataSize();

    public abstract double getMeasurementDataIndexSize();

    public abstract void execCreateOperation(Object content);

    public abstract void execInsertOperation(Object content);

    public abstract void execSelectOperation(Object content);

    public abstract void execDeleteOperation(Object content);

    public void registerObserver(IObserver observer)
    {
        observers.add(observer);
    }

    public void removeObserver(IObserver observer)
    {
        observers.remove(observer);

    }

    public void notifyObservers()
    {}

}