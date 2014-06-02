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

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.talend.colibri.files.ResourcesUtils;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class TablesCreation {

    private static Logger log = Logger.getLogger(TablesCreation.class);

    public static void modelTableMigration() {
        // Add importduration & executionduration columns in jobs_stats table:
        boolean needUpdate = false;
        try {
            DbUtils.executeQuery("SELECT importduration, executionduration FROM jobs_stats");
        } catch (SQLException e1) {
            needUpdate = true;
        }
        if (needUpdate) {
            log.info("Migration jobs_stats table (adding importduration & executionduration columns)...");
            final String migrationQuery = "ALTER TABLE jobs_stats ADD (`importduration` int(10) default NULL, `executionduration` int(10) default NULL)";
            try {
                DbUtils.executeQuery(migrationQuery);
            } catch (SQLException e) {
                log.error(e.getMessage() + ", the current query is :\n" + migrationQuery + "\n", e);
            }
        }
    }

    public static void modelTablesCreation() {
        boolean needTableCreation = false;
        try {
            DbUtils.executeQuery("SELECT count(*) FROM status");
        } catch (SQLException e1) {
            needTableCreation = true;
        }
        if (needTableCreation) {
            log.info("Creating tables...");

            String sqlScript = "";
            String str;
            String currentQuery = null;
            try {
                String filename = "createTables.sql";
                BufferedReader in = new BufferedReader(new FileReader(ResourcesUtils.getResource(filename)));
                while ((str = in.readLine()) != null) {
                    if (!str.startsWith("#")) {
                        sqlScript += "\n" + str;
                    }
                }

                String[] sqltable = sqlScript.split(";");
                for (String sql : sqltable) {
                    if (sql.trim().compareTo("") != 0) {
                        currentQuery = sql;
                        DbUtils.executeQuery(sql);
                    }
                }
                in.close();
            } catch (Throwable e) {
                log.error(e.getMessage() + ", the current query is :\n" + currentQuery + "\n", e);
            }
        }
        modelTableMigration();
    }

    public static void tempResultTableCreation(String branch) throws SQLException {
        String tableName = DbUtils.getResultTableForBranch(branch);
        log.info("Creating table " + tableName);
        String query = "DROP TABLE IF EXISTS " + tableName;
        DbUtils.executeQuery(query);
        query = "CREATE TABLE `" + tableName + "` (`moment` datetime default NULL, " + "`pid` varchar(20) default NULL, "
                + "`project` varchar(50) default NULL, " + "`job` varchar(100) default NULL, "
                + "`language` varchar(5) default NULL, " + "`origin` varchar(50) default NULL, "
                + "`status` varchar(10) default NULL, " + "`substatus` varchar(255) default NULL, "
                + "`description` varchar(255) default NULL) ENGINE=MyISAM DEFAULT CHARSET=latin1";
        DbUtils.executeQuery(query);
    }

    public static void main(String[] args) throws SQLException {
        modelTablesCreation();
        // tempResultTableCreation("branches/branch-3_2");
    }
}
