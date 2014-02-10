package com.itc;

import java.io.File;

public abstract class IObserver
{
    protected String logFilePath = null;
    protected File logFile = null;
    protected IStorageManager mysqlManager = null;
    protected IStorageManager mongoDBManager = null;

    public IObserver()
    {
        this.mysqlManager = new MySQLManager();
        this.mongoDBManager = new MongoDBManager();
    }

    protected void initLogFile()
    {
        this.logFile = new File(logFilePath);
    }

    public abstract void update(String msg, double interest);

    public abstract void update(String msg);

    public void clearsysLog()
    {
        if (this.logFile.exists())
        {
            this.logFile.delete();
        }
    }

    @SuppressWarnings("restriction")
    public String checkCPUInfo()
    {
        com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean)
                java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return "Available processors: " + os.getAvailableProcessors() +
                "; System avg load: " + os.getSystemLoadAverage() + "; ";
    }

    @SuppressWarnings("restriction")
    public String checkRAMInfo()
    {
        com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean)
                java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        long physicalMemorySize = os.getTotalPhysicalMemorySize() - os.getFreePhysicalMemorySize();
        return "memoryUsage (MB): (" + (physicalMemorySize / 1024 / 1024) +
                "/" + (os.getTotalPhysicalMemorySize() / 1024 / 1024) + "; ";
    }

    public String checkMysqlDBSizeInfo()
    {
        return "MySQL DATA SIZE (MB): " + this.mysqlManager.getMeasurementDataSize() + "; TOTAL INDEX SIZE (KB): "
                + this.mysqlManager.getMeasurementDataIndexSize();
    }

    public String checkMongoDBSizeInfo()
    {
        return "MongoDB DATA SIZE: " + this.mongoDBManager.getMeasurementDataSize() + "; TOTAL INDEX SIZE (KB): "
                + this.mongoDBManager.getMeasurementDataIndexSize();
    }
}
