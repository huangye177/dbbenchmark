package org.itc.scenario;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class StaticObserver extends IObserver
{
    public StaticObserver()
    {
        super();
        logFilePath = "./staticlog.txt";

        this.initLogFile();
    }

    /**
     * Update message-observer with message string content and numeric values
     */
    @Override
    public void update(String msg, double value)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        // TODO refactoring
        formatter.setTimeZone(TimeZone.getTimeZone("CET"));

        String timeStamp = formatter.format(Calendar.getInstance().getTime());

        String infoLine = "\n" + timeStamp + " \n " + msg + " | " + value;
        infoLine += " #CPU: " + this.checkCPUInfo() +
                "; #RAM: " + this.checkRAMInfo() +
                "; #MySQL Size: " + this.checkMysqlDBSizeInfo() +
                "; #MongoDB Size: " + this.checkMongoDBSizeInfo();

        System.out.println(infoLine);

        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new FileWriter(this.logFile, true), 32768);
            out.write(infoLine + "\n\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    /**
     * Update message-observer with message string content
     */
    @Override
    public void update(String msg)
    {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
        String infoLine = "" + timeStamp + " \n\n" + msg;

        System.out.println(infoLine);
        BufferedWriter out = null;

        try
        {
            out = new BufferedWriter(new FileWriter(this.logFile, true), 32768);
            out.write(infoLine + "\n\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
}
