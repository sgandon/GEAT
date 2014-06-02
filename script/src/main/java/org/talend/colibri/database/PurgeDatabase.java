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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class PurgeDatabase {

    private static Logger log = Logger.getLogger(PurgeDatabase.class);

    public static void purge(String branch, String timestamp) throws SQLException {
        ResultSet executeQuery = DbUtils.executeQueryWithReturn("SELECT id from executions WHERE branch='" + branch
                + "' AND revision=" + timestamp);
        if (executeQuery.first())
            purgeTables(executeQuery.getInt("id"));
    }

    private static void purgeTables(int idExec) throws SQLException {
        log.info("Execution already done -> Purge tables");
        String[] tablesToPurge = new String[] { "components_jobs", "exec_stats", "jobs_changes", "results_def" };
        for (String current : tablesToPurge) {
            DbUtils.executeQuery("DELETE FROM " + current + " WHERE id_exec=" + idExec);
        }
    }

}
