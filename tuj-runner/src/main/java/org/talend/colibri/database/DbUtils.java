// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.colibri.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.gjt.mm.mysql.Driver;
import org.talend.colibri.properties.Bundles;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class DbUtils {

    private static Logger log = Logger.getLogger(DbUtils.class);

    private static Driver driver;

    private static Connection connection;

    protected static Driver getDriver() throws SQLException {
        if (driver == null) {
            driver = new Driver();
            log.debug("Instancing database driver: " + driver.getClass().getCanonicalName());
        }
        return driver;
    }

    public static Connection getConnection() throws SQLException {
        if (connection != null && connection.isClosed())
            connection = null;
        if (connection == null) {
            log.debug("Connecting to dabase...");
            String url = Bundles.getString("database.url");
            String userName = Bundles.getString("database.userName");
            String password = Bundles.getString("database.password");

            Properties props = new Properties();
            props.setProperty("user", userName);
            props.setProperty("password", password);

            connection = getDriver().connect(url, props);
            log.info("Connection to database ok");
        }
        return connection;
    }

    public static void testConnection() throws SQLException {
        log.info("Test connection to database");
        DbUtils.getConnection();
    }

    public static void executeQuery(String currentQuery) throws SQLException {
        if (currentQuery == null) {
            throw new IllegalArgumentException();
        }

        log.trace("Executing query: [" + currentQuery + "]");
        PreparedStatement prepareStatement = null;
        try {
            prepareStatement = DbUtils.getConnection().prepareStatement(currentQuery);
            prepareStatement.execute(currentQuery);
        } finally {
            if (prepareStatement != null)
                prepareStatement.close();
        }

    }

    public static ResultSet executeQueryWithReturn(String currentQuery) throws SQLException {
        if (currentQuery == null) {
            throw new IllegalArgumentException();
        }

        log.trace("Executing query: [" + currentQuery + "]");
        Statement prepareStatement = null;
        try {
            prepareStatement = DbUtils.getConnection().createStatement();
            return prepareStatement.executeQuery(currentQuery);
        } finally {

        }
    }

    public static String getResultTableForBranch(String branch) {
        return Bundles.getString("jobs.resultdatabase.table") + branch.replace('.', '_');
    }

    private static Integer idExec = null;

    public static int getIdExec(String branch, String timestamp) throws SQLException {
        if (idExec == null) {
            String query;
            query = "SELECT id FROM executions WHERE branch='" + branch + "' AND revision=" + timestamp;
            ResultSet resultSet = DbUtils.executeQueryWithReturn(query);
            if (resultSet.first()) {
                idExec = resultSet.getInt(1);
            } else
                throw new SQLException();
            resultSet.close();
        }
        return idExec;
    }

    public static int getIdJob(String lanuage, String name) throws SQLException {
        String query = "SELECT id FROM jobs WHERE language='" + lanuage + "' AND name='" + name + "'";
        ResultSet resultSet = null;
        try {
            resultSet = DbUtils.executeQueryWithReturn(query);
            if (resultSet.first()) {
                return resultSet.getInt(1);
            } else
                throw new SQLException();
        } finally {
            if (resultSet != null)
                resultSet.close();
        }
    }
}
