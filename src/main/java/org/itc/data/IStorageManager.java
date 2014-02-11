package org.itc.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.itc.scenario.IObserver;

public abstract class IStorageManager
{
    final protected int amount_of_dataseries = 3000;
    final private int amount_of_project = 10000;
    final private int amount_of_day_of_year_2013 = 365;
    final private int value_of_measurement = 1000;

    protected boolean primaryIdAutoIncrement = true;

    private Random generator = new Random();
    private SimpleDateFormat format = new SimpleDateFormat("yyyyD");

    private ArrayList<IObserver> observers = new ArrayList<IObserver>();

    // private long indexFetchCounter = 0;
    // private long nonindexFetchCounter = 0;

    final protected double printthreshold = 0.1;
    protected double currentThreshold = 0.1;

    protected boolean isTableSharding = false;

    protected int partition = 1;
    protected String partitionId = "id";

    public IStorageManager()
    {
        this.currentThreshold = 0.1;
    }

    public abstract void initConnection();

    public abstract void closeConnection();

    public abstract void createMeasurementTable();

    public abstract void insertMeasurements();

    public abstract void insertMeasurementsInBatch();

    public abstract long[] selectMeasurementByDataSeriesId();

    public abstract long[] selectMeasurementByProjectId();

    public abstract void dropMeasurementTable();

    public abstract double getMeasurementDataSize();

    public abstract double getMeasurementDataIndexSize();

    public void registerObserver(IObserver observer)
    {
        observers.add(observer);
    }

    public void removeObserver(IObserver observer)
    {
        observers.remove(observer);

    }

    public void notifyObservers()
    {
        // if (this.indexFetchCounter == 0)
        // {
        // /*
        // * now it should be query with NO index
        // */
        // if (((double) this.nonindexFetchCounter / this.amount_of_records) <
        // this.currentThreshold)
        // {
        // // nothing wrong or sth wrong, lol
        // }
        // else
        // {
        // this.currentThreshold += this.printthreshold;
        // String msg = "Query_withOUT_index, current executed queries: ";
        //
        // for (IObserver ob : observers)
        // {
        // ob.update(msg, nonindexFetchCounter);
        // }
        // }
        //
        // }
    }

    protected int getProjectId()
    {
        return this.generator.nextInt(this.amount_of_project);
    }

    protected int getDataSeriesId()
    {
        return this.generator.nextInt(this.amount_of_dataseries);
    }

    protected Date getMeasDate()
    {
        String julianDate = "2013" + this.generator.nextInt(this.amount_of_day_of_year_2013);

        try
        {
            Date parseDate = this.format.parse(julianDate);
            return parseDate;
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    protected double getMeasValue()
    {
        return this.generator.nextDouble() * value_of_measurement;
    }

    public boolean isMultiThreadAllowed()
    {
        return this.primaryIdAutoIncrement;
    }

}