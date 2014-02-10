package com.itc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MySQLConnectionManager
{
    protected static final Log logger = LogFactory.getLog(MySQLConnectionManager.class);

    public static Connection getDBConnection(String connString, String userName, String password)
    {
        Connection conn = null;

        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            logger.error("ClassNotFoundException: " + e.toString());
            e.printStackTrace();
        }

        try
        {
            conn = DriverManager.getConnection(connString, userName, password);
        }
        catch (SQLException e)
        {
            logger.error("SQLException: " + e.toString());
            e.printStackTrace();
        }

        return conn;

    }

    public static void closeDBConnection(Connection conn)
    {
        try
        {
            conn.close();
        }
        catch (SQLException e)
        {
            logger.error("SQLException: " + e.toString());
            e.printStackTrace();
        }
    }
}
