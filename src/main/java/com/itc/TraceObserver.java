package com.itc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class TraceObserver extends IObserver
{

    public TraceObserver()
    {
        super();
        logFilePath = System.getProperty("user.dir") + "/tracelog.txt";

        this.initLogFile();
    }

    @Override
    public void update(String msg, double value)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("CET"));

        String timeStamp = formatter.format(Calendar.getInstance().getTime());
        String infoLine = "" + timeStamp + " | " + msg + " | " + value;
        infoLine += "\n#CPU: " + this.checkCPUInfo() +
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

    @Override
    public void update(String msg)
    {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
        String infoLine = "" + timeStamp + " \n " + msg;

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
