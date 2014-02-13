package org.itc.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQLManager extends IStorageManager
{

    private Connection conn = null;

    /**
     * GRANT CREATE,SELECT,INSERT,DELETE,UPDATE,ALTER,DROP ON iristestdb.* TO
     * 'iristestdb'@'localhost' IDENTIFIED BY 'iristestdb'; FLUSH PRIVILEGES;
     */
    final private String connString = "jdbc:mysql://127.0.0.1:3306/iristestdb";
    final private String userName = "iristestdb";
    final private String password = "iristestdb";

    private long amount_of_records = 0;

    public MySQLManager()
    {

    }

    public MySQLManager(long amount_of_records, boolean isTableSharding, int partition)
    {
        super();
        this.amount_of_records = amount_of_records;
        this.isTableSharding = isTableSharding;
        this.partition = partition;

        if (partition < 1)
        {
            System.out.println("The Data partition must be bigger than 1");
            System.exit(0);
        }
    }

    @Override
    public void initConnection()
    {
        this.conn = MySQLConnectionManager.getDBConnection(this.connString, this.userName, this.password);
        // this.printDBMetaInfo();
    }

    @Override
    public void closeConnection()
    {
        MySQLConnectionManager.closeDBConnection(this.conn);
    }

    @Override
    public void createMeasurementTable()
    {
        // System.out.println("CREATING MySQL Tables...");

        List<String> tableCreationSQL = new ArrayList<String>();

        if (this.isTableSharding)
        {
            String createStr = null;

            for (int i = 0; i < this.amount_of_dataseries; i++)
            {
                long dsId = i;

                if (this.primaryIdAutoIncrement)
                {
                    createStr = "CREATE TABLE `gm_std_measurements_" + dsId + "` (\n" +
                            "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                            "  `project_id` int(11) DEFAULT NULL,\n" +
                            "  `fkDataSeriesId` bigint(20) DEFAULT NULL,\n" +
                            "  `measDateUtc` datetime DEFAULT NULL,\n" +
                            "  `measDateSite` datetime DEFAULT NULL,\n" +
                            "  `measvalue` double DEFAULT NULL,\n" +
                            "  `refMeas` bit(1) DEFAULT NULL,\n" +
                            "  `reliability` double DEFAULT NULL,\n" +
                            "  PRIMARY KEY (`id`),\n" +
                            // "  KEY `Index1` (`fkDataSeriesId`),\n" +
                            "  KEY `Index2` (`fkDataSeriesId`,`measDateUtc`)\n" +
                            // "  KEY `Index3` (`measDateUtc`)\n" +
                            ") ENGINE=MyISAM DEFAULT CHARSET=utf8;";

                }
                else
                {
                    createStr = "CREATE TABLE `gm_std_measurements_" + dsId + "` (\n" +
                            "  `id` bigint(20) NOT NULL,\n" +
                            "  `project_id` int(11) DEFAULT NULL,\n" +
                            "  `fkDataSeriesId` bigint(20) DEFAULT NULL,\n" +
                            "  `measDateUtc` datetime DEFAULT NULL,\n" +
                            "  `measDateSite` datetime DEFAULT NULL,\n" +
                            "  `measvalue` double DEFAULT NULL,\n" +
                            "  `refMeas` bit(1) DEFAULT NULL,\n" +
                            "  `reliability` double DEFAULT NULL,\n" +
                            "  PRIMARY KEY (`id`),\n" +
                            // "  KEY `Index1` (`fkDataSeriesId`),\n" +
                            "  KEY `Index2` (`fkDataSeriesId`,`measDateUtc`)\n" +
                            // "  KEY `Index3` (`measDateUtc`)\n" +
                            ") ENGINE=MyISAM DEFAULT CHARSET=utf8;";

                }

                tableCreationSQL.add(createStr);
            }
        }
        else
        {
            String createStr = null;

            if (this.primaryIdAutoIncrement)
            {
                createStr = "CREATE TABLE `gm_std_measurements` (\n" +
                        "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                        "  `project_id` int(11) DEFAULT NULL,\n" +
                        "  `fkDataSeriesId` bigint(20) DEFAULT NULL,\n" +
                        "  `measDateUtc` datetime DEFAULT NULL,\n" +
                        "  `measDateSite` datetime DEFAULT NULL,\n" +
                        "  `measvalue` double DEFAULT NULL,\n" +
                        "  `refMeas` bit(1) DEFAULT NULL,\n" +
                        "  `reliability` double DEFAULT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        // "  KEY `Index1` (`fkDataSeriesId`),\n" +
                        "  KEY `Index2` (`fkDataSeriesId`,`measDateUtc`)\n" +
                        // "  KEY `Index3` (`measDateUtc`)\n" +
                        ") ENGINE=MyISAM DEFAULT CHARSET=utf8 \n";

            }
            else
            {
                createStr = "CREATE TABLE `gm_std_measurements` (\n" +
                        "  `id` bigint(20) NOT NULL,\n" +
                        "  `project_id` int(11) DEFAULT NULL,\n" +
                        "  `fkDataSeriesId` bigint(20) DEFAULT NULL,\n" +
                        "  `measDateUtc` datetime DEFAULT NULL,\n" +
                        "  `measDateSite` datetime DEFAULT NULL,\n" +
                        "  `measvalue` double DEFAULT NULL,\n" +
                        "  `refMeas` bit(1) DEFAULT NULL,\n" +
                        "  `reliability` double DEFAULT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        // "  KEY `Index1` (`fkDataSeriesId`),\n" +
                        "  KEY `Index2` (`fkDataSeriesId`,`measDateUtc`)\n" +
                        // "  KEY `Index3` (`measDateUtc`)\n" +
                        ") ENGINE=MyISAM DEFAULT CHARSET=utf8 \n";

            }

            if (this.partition == 1)
            {
                createStr += ";";
            }
            else
            {
                /**************************************************
                 * Partition Id: id START
                 **************************************************/
                createStr += "PARTITION BY RANGE (id) ( \n";
                long chunckSize = this.amount_of_records / this.partition;
                int partitionNumber = 0;
                for (int j = 0; j < this.partition - 1; j++)
                {
                    partitionNumber = j;
                    createStr += "PARTITION p" + partitionNumber +
                            " VALUES LESS THAN (" + (chunckSize * (j + 1)) + "), \n";
                }
                createStr += "PARTITION p" + (partitionNumber + 1) +
                        " VALUES LESS THAN MAXVALUE);";

                /************************************
                 * Partition Id END
                 ***********************************/

                /****************************************************
                 * Partition Id: fkDataSeriesId, then NO PK allowed
                 ****************************************************/
                // createStr = "CREATE TABLE `gm_std_measurements` (\n" +
                // "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                // "  `project_id` int(11) DEFAULT NULL,\n" +
                // "  `fkDataSeriesId` bigint(20) DEFAULT NULL,\n" +
                // "  `measDateUtc` datetime DEFAULT NULL,\n" +
                // "  `measDateSite` datetime DEFAULT NULL,\n" +
                // "  `measvalue` double DEFAULT NULL,\n" +
                // "  `refMeas` bit(1) DEFAULT NULL,\n" +
                // "  `reliability` double DEFAULT NULL,\n" +
                // "  KEY `Index_id` (`id`),\n" +
                // "  KEY `Index2` (`fkDataSeriesId`,`measDateUtc`)\n" +
                // ") ENGINE=MyISAM DEFAULT CHARSET=utf8 \n";
                //
                // createStr += "PARTITION BY RANGE (fkDataSeriesId) ( \n";
                // long chunckSize = this.amount_of_dataseries / this.partition;
                // int partitionNumber = 0;
                // for (int j = 0; j < this.partition - 1; j++)
                // {
                // partitionNumber = j;
                // createStr += "PARTITION p" + partitionNumber +
                // " VALUES LESS THAN (" + (chunckSize * (j + 1)) + "), \n";
                // }
                // createStr += "PARTITION p" + (partitionNumber + 1) +
                // " VALUES LESS THAN MAXVALUE);";

                /************************************
                 * Partition fkDataSeriesId: END
                 ***********************************/

            }

            tableCreationSQL.add(createStr);
        }

        /*
         * create one or multi-sharding tables
         */
        for (String createStr : tableCreationSQL)
        {
            Statement stmt = null;

            try
            {
                stmt = this.conn.createStatement();

                stmt.executeUpdate(createStr);
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
                    stmt.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void insertMeasurements()
    {
        for (int i = 0; i < this.amount_of_records; i++)
        {
            Date measDate = this.getMeasDate();
            this.insertMeasurement(i, this.getProjectId(), this.getDataSeriesId(), measDate, measDate, this.getMeasValue());
        }
    }

    /**
     * Insert-in-Batch only supports Auto-incremental with NO-table-sharding
     * mode
     */
    @Override
    public void insertMeasurementsInBatch()
    {
        /*
         * initialize to-execute insert statements grouped by dataseries Id
         */
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        HashMap<Integer, List<String>> toExecuteInsertStatments = new HashMap<Integer, List<String>>();
        int batchCommitsize = 10000;

        int chunckSize = this.amount_of_dataseries / this.partition;
        for (int j = 0; j < this.partition; j++)
        {
            toExecuteInsertStatments.put(j, new ArrayList<String>());
        }

        int numOfInsertStr = 0;

        for (int i = 0; i < this.amount_of_records; i++)
        {

            Date measDate = this.getMeasDate();
            int dsId = this.getDataSeriesId();

            String insertStr = "INSERT INTO `gm_std_measurements`\n" +
                    "(`project_id`,\n" +
                    "`fkDataSeriesId`,\n" +
                    "`measDateUtc`,\n" +
                    "`measDateSite`,\n" +
                    "`measvalue`,\n" +
                    "`refMeas`,\n" +
                    "`reliability`)\n" +
                    "VALUES\n" +
                    "(" + this.getProjectId() + ",\n" +
                    dsId + ",\n" +
                    dateFormat.format(measDate) + ",\n" +
                    dateFormat.format(measDate) + ",\n" +
                    this.getMeasValue() + ",\n" +
                    "0,\n" +
                    "1.0\n" +
                    ");";

            int chunckIndex = dsId / chunckSize;

            if (numOfInsertStr == batchCommitsize)
            {
                toExecuteInsertStatments.get(chunckIndex).add(insertStr);
                this.insertMeasurement(toExecuteInsertStatments);

                /*
                 * clean up the insert String by HashMap group, in order to add
                 * more insert sql statements
                 */
                for (int j = 0; j < this.partition; j++)
                {
                    toExecuteInsertStatments.get(j).clear();
                    toExecuteInsertStatments.put(j, new ArrayList<String>());
                }

                numOfInsertStr = 0;
            }
            else
            {
                toExecuteInsertStatments.get(chunckIndex).add(insertStr);
                numOfInsertStr++;
            }
        }

        if (toExecuteInsertStatments.size() > 0)
        {
            this.insertMeasurement(toExecuteInsertStatments);

            /*
             * clean up the insert String by HashMap group, in order to add more
             * insert sql statements
             */
            for (int j = 0; j < this.partition; j++)
            {
                toExecuteInsertStatments.get(j).clear();
            }
        }
    }

    @Override
    public long[] selectMeasurementByDataSeriesId()
    {
        long queryTime = System.currentTimeMillis();
        long queryFetchTime = System.currentTimeMillis();

        int dsId = this.getDataSeriesId();

        String selectStr = "";
        if (this.isTableSharding)
        {
            selectStr = "SELECT id, project_id, fkDataSeriesId, measDateUtc, measvalue FROM gm_std_measurements_" + dsId + " " +
                    "WHERE fkDataSeriesId=?;";
        }
        else
        {
            selectStr = "SELECT id, project_id, fkDataSeriesId, measDateUtc, measvalue FROM gm_std_measurements " +
                    "WHERE fkDataSeriesId=?;";
        }

        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try
        {
            preparedStatement = this.conn.prepareStatement(selectStr);
            preparedStatement.setInt(1, dsId);

            /*
             * Set query cursor
             */
            preparedStatement.setFetchSize(100);

            rs = preparedStatement.executeQuery();

            queryTime = System.currentTimeMillis() - queryTime;

            while (rs.next())
            {
                String str = "";
                str += rs.getLong("id");
                str += rs.getInt("project_id");
                str += rs.getLong("fkDataSeriesId");
                str += rs.getDate("measDateUtc");
                str += rs.getDouble("measvalue");
                // System.out.println("$$$ " + str);
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
                preparedStatement.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        queryFetchTime = System.currentTimeMillis() - queryFetchTime;

        return new long[] { queryTime, queryFetchTime };
    }

    @Deprecated
    @Override
    public long[] selectMeasurementByProjectId()
    {
        long queryTime = System.currentTimeMillis();
        long queryFetchTime = System.currentTimeMillis();

        String selectStr = "SELECT id, project_id, fkDataSeriesId, measDateUtc, measvalue FROM gm_std_measurements " +
                "WHERE project_id=?;";

        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try
        {
            preparedStatement = this.conn.prepareStatement(selectStr);
            preparedStatement.setInt(1, this.getProjectId());

            /*
             * Set query cursor
             */
            preparedStatement.setFetchSize(100);

            rs = preparedStatement.executeQuery();

            queryTime = System.currentTimeMillis() - queryTime;

            while (rs.next())
            {
                String str = "";
                str += rs.getLong("id");
                str += rs.getInt("project_id");
                str += rs.getLong("fkDataSeriesId");
                str += rs.getDate("measDateUtc");
                str += rs.getDouble("measvalue");
                // System.out.println("$$$ " + str);
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
                preparedStatement.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        queryFetchTime = System.currentTimeMillis() - queryFetchTime;

        return new long[] { queryTime, queryFetchTime };
    }

    @Override
    public void dropMeasurementTable()
    {
        List<String> deleteStrList = new ArrayList<String>();

        if (this.isTableSharding)
        {
            for (int i = 0; i < this.amount_of_dataseries; i++)
            {
                String deleteStr = "DROP TABLE gm_std_measurements_" + i + ";";
                deleteStrList.add(deleteStr);
            }
        }
        else
        {
            String deleteStr = "DROP TABLE gm_std_measurements;";
            deleteStrList.add(deleteStr);
        }

        /*
         * delete one or multi- created tables/table-shards
         */
        for (String deleteStr : deleteStrList)
        {
            Statement stmt = null;

            try
            {
                stmt = this.conn.createStatement();

                stmt.executeUpdate(deleteStr);
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
                    stmt.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
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

    /************************
     * private util methods
     ************************/

    private void insertMeasurement(long id, int projectId, long fkDataSeriesId, Date measDateUtc, Date measDateSite, double measvalue)
    {
        String insertStr = null;

        if (this.isTableSharding)
        {
            if (this.primaryIdAutoIncrement)
            {
                insertStr = "INSERT INTO `gm_std_measurements_" + fkDataSeriesId + "`\n" +
                        "(`project_id`,\n" +
                        "`fkDataSeriesId`,\n" +
                        "`measDateUtc`,\n" +
                        "`measDateSite`,\n" +
                        "`measvalue`,\n" +
                        "`refMeas`,\n" +
                        "`reliability`)\n" +
                        "VALUES\n" +
                        "(\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "0,\n" +
                        "1.0\n" +
                        ");";
            }
            else
            {
                insertStr = "INSERT INTO `gm_std_measurements_" + fkDataSeriesId + "`\n" +
                        "(`id`,\n" +
                        "`project_id`,\n" +
                        "`fkDataSeriesId`,\n" +
                        "`measDateUtc`,\n" +
                        "`measDateSite`,\n" +
                        "`measvalue`,\n" +
                        "`refMeas`,\n" +
                        "`reliability`)\n" +
                        "VALUES\n" +
                        "(\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "0,\n" +
                        "1.0\n" +
                        ");";
            }
        }
        else
        {
            if (this.primaryIdAutoIncrement)
            {
                insertStr = "INSERT INTO `gm_std_measurements`\n" +
                        "(`project_id`,\n" +
                        "`fkDataSeriesId`,\n" +
                        "`measDateUtc`,\n" +
                        "`measDateSite`,\n" +
                        "`measvalue`,\n" +
                        "`refMeas`,\n" +
                        "`reliability`)\n" +
                        "VALUES\n" +
                        "(\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "0,\n" +
                        "1.0\n" +
                        ");";
            }
            else
            {
                insertStr = "INSERT INTO `gm_std_measurements`\n" +
                        "(`id`,\n" +
                        "`project_id`,\n" +
                        "`fkDataSeriesId`,\n" +
                        "`measDateUtc`,\n" +
                        "`measDateSite`,\n" +
                        "`measvalue`,\n" +
                        "`refMeas`,\n" +
                        "`reliability`)\n" +
                        "VALUES\n" +
                        "(\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "?,\n" +
                        "0,\n" +
                        "1.0\n" +
                        ");";
            }
        }

        PreparedStatement preparedStatement = null;

        try
        {
            preparedStatement = this.conn.prepareStatement(insertStr);
            if (this.primaryIdAutoIncrement)
            {
                preparedStatement.setLong(1, projectId);
                preparedStatement.setLong(2, fkDataSeriesId);
                preparedStatement.setDate(3, this.getSQLDate(measDateUtc));
                preparedStatement.setDate(4, this.getSQLDate(measDateSite));
                preparedStatement.setDouble(5, measvalue);
            }
            else
            {
                preparedStatement.setLong(1, id);
                preparedStatement.setLong(2, projectId);
                preparedStatement.setLong(3, fkDataSeriesId);
                preparedStatement.setDate(4, this.getSQLDate(measDateUtc));
                preparedStatement.setDate(5, this.getSQLDate(measDateSite));
                preparedStatement.setDouble(6, measvalue);
            }

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

    private void insertMeasurement(HashMap<Integer, List<String>> toExecuteInsertStatments)
    {
        // System.out.println("INSERT BATCH COMMIT: " + new Date());
        // String logFilePath = System.getProperty("user.dir") +
        // "/tracelog.txt";
        // File logFile = new File(logFilePath);

        Statement statement = null;

        try
        {
            /*
             * set auto-commit mode to false to allow execution in batch
             */
            this.conn.setAutoCommit(false);

            statement = this.conn.createStatement();

            for (Map.Entry<Integer, List<String>> entry : toExecuteInsertStatments.entrySet())
            {
                List<String> sqlStatementList = entry.getValue();

                /*
                 * insert all records for the same DataSeries in batch mode
                 */
                for (String sqlStatement : sqlStatementList)
                {
                    // /*
                    // * TESTING CODE START
                    // */
                    // System.out.println(sqlStatement.replaceAll("\n", " "));
                    // BufferedWriter out = null;
                    //
                    // try
                    // {
                    // out = new BufferedWriter(new FileWriter(logFile, true),
                    // 32768);
                    // out.write("KEY: " + entry.getKey() + "; " +
                    // sqlStatement.replaceAll("\n", " ") + "\n");
                    // }
                    // catch (IOException e)
                    // {
                    // e.printStackTrace();
                    // }
                    // finally
                    // {
                    // try
                    // {
                    // out.close();
                    // }
                    // catch (IOException e)
                    // {
                    // e.printStackTrace();
                    // }
                    // }
                    // /*
                    // * TESTING CODE END
                    // */

                    statement.addBatch(sqlStatement);
                }
                statement.executeBatch();
                conn.commit();

                statement.clearBatch();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                statement.close();

                /*
                 * set auto-commit mode back to true
                 */
                this.conn.setAutoCommit(true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

    }

    private java.sql.Date getSQLDate(java.util.Date date)
    {
        return new java.sql.Date(date.getTime());
    }

}