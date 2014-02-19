package org.itc.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLManager extends IStorageManager
{
    private Connection conn = null;
    private Statement stmt = null;

    private String dbConnString = "";
    private String dbUserName = "";
    private String dbPassword = "";

    public MySQLManager(String... args)
    {
        if (args.length >= 1 && args[0] != null)
        {
            this.dbConnString = args[0];
        }

        if (args.length >= 2 && args[1] != null)
        {
            this.dbUserName = args[1];
        }

        if (args.length >= 3 && args[2] != null)
        {
            this.dbPassword = args[2];
        }
    }

    @Override
    public void initConnection()
    {
        this.conn = MySQLConnectionManager.getDBConnection(this.dbConnString, this.dbUserName, this.dbPassword);
        try
        {
            this.stmt = this.conn.createStatement();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        this.printDBMetaInfo();
    }

    @Override
    public void closeConnection()
    {
        if (this.stmt != null)
        {
            try
            {
                this.stmt.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        this.stmt = null;
        MySQLConnectionManager.closeDBConnection(this.conn);
    }

    @Override
    public void execSelectOperation(Object content)
    {
        ResultSet rs = null;

        try
        {
            rs = this.stmt.executeQuery((String) content);

            while (rs.next())
            {
                rs.toString();
            }

            this.notifyObservers();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // finally block used to close resources
            try
            {
                rs.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void execCreateOperation(Object content)
    {
        this.execPreparedStatementExecuteUpdate(content);
    }

    @Override
    public void execInsertOperation(Object content)
    {
        this.execPreparedStatementExecuteUpdate(content);
    }

    @Override
    public void execDeleteOperation(Object content)
    {
        this.execPreparedStatementExecuteUpdate(content);
    }

    private void execPreparedStatementExecuteUpdate(Object content)
    {
        PreparedStatement preparedStatement = null;

        try
        {
            preparedStatement = this.conn.prepareStatement((String) content);

            preparedStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // finally block used to close resources
            try
            {
                preparedStatement.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void printDBMetaInfo()
    {
        try
        {
            DatabaseMetaData dbmd = this.conn.getMetaData();

            System.out.println("\nConnected with " +
                    dbmd.getDriverName() + " with DriverVersion: " + dbmd.getDriverVersion()
                    + "{ " + dbmd.getDriverMajorVersion() + "," +
                    dbmd.getDriverMinorVersion() + " }" + " to DataBase: " +
                    dbmd.getDatabaseProductName() + " " +
                    dbmd.getDatabaseProductVersion() + "\n");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public double getMeasurementDataSize()
    {
        String queryStr = "SELECT table_schema as \"dbname\", \n" +
                "data_length / 1024 as \"dbsize_mb\" \n" +
                "FROM information_schema.TABLES \n" +
                "WHERE TABLE_SCHEMA='iristestdb';";

        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        double dbSize = 0.0;

        if (this.conn == null)
        {
            /*
             * No db created yet
             */
            return dbSize;
        }

        try
        {
            preparedStatement = this.conn.prepareStatement(queryStr);

            /*
             * Set query cursor
             */
            rs = preparedStatement.executeQuery();

            while (rs.next())
            {
                dbSize = rs.getDouble("dbsize_mb");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // finally block used to close resources
            try
            {
                rs.close();
                preparedStatement.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        return dbSize;

    }

    @Override
    public double getMeasurementDataIndexSize()
    {
        String queryStr = "SELECT table_schema as \"dbname\", \n" +
                "index_length / 1024 as \"dbindexsize_mb\" \n" +
                "FROM information_schema.TABLES \n" +
                "WHERE TABLE_SCHEMA='iristestdb';";

        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        double dbIndexSize = 0.0;

        if (this.conn == null)
        {
            /*
             * No db created yet
             */
            return dbIndexSize;
        }

        try
        {
            preparedStatement = this.conn.prepareStatement(queryStr);

            /*
             * Set query cursor
             */
            rs = preparedStatement.executeQuery();

            while (rs.next())
            {
                dbIndexSize = rs.getDouble("dbindexsize_mb");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // finally block used to close resources
            try
            {
                rs.close();
                preparedStatement.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        return dbIndexSize;
    }

    private java.sql.Date getSQLDate(java.util.Date date)
    {
        return new java.sql.Date(date.getTime());
    }
}
