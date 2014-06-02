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
package org.talend.colibri.archive;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.talend.colibri.database.DbUtils;
import org.talend.colibri.properties.Bundles;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class ArchiveResults {

    private static Logger log = Logger.getLogger(ArchiveResults.class);

    public static void archive(String branch, String timestamp) throws SQLException {
        log.info("Archive results");
        String branchTable = DbUtils.getResultTableForBranch(branch);

        // Jobs:
        String query = "INSERT INTO jobs (name, language, id_validation) SELECT DISTINCT(job), r.language, "
                + Bundles.getString("jobs.defaultStatus") + " FROM " + branchTable + " r "
                + "LEFT JOIN jobs ON name=job AND jobs.language=r.language WHERE name IS NULL";
        DbUtils.executeQuery(query);

        // Copy records from temp results table to definitive table:
        // -> Replace job name and language by id_job
        // -> Replace branch and revision by id_exec
        // -> Replace substatus by id_status
        query = "UPDATE " + branchTable + " SET substatus='Timeout' WHERE substatus='Error code=143'";
        DbUtils.executeQuery(query);

        query = "UPDATE " + branchTable + " SET substatus='Unknown' WHERE substatus='???'";
        DbUtils.executeQuery(query);

        query = "UPDATE " + branchTable + " SET substatus='Success' WHERE substatus='Ok'";
        DbUtils.executeQuery(query);

        query = "INSERT INTO results_def SELECT r.moment, r.origin, r.description, ex.id id_exec, st.id id_status, j.id id_job "
                + "FROM " + branchTable + " r, executions ex, jobs j, status st " + "WHERE ex.branch='" + branch
                + "' AND ex.revision="
                + timestamp
                + " AND r.job=j.name AND r.language=j.language AND r.substatus=st.sub_status";
        DbUtils.executeQuery(query);

        // Log the non-treated status before drop the table:
        query = "SELECT DISTINCT(substatus), COUNT(*) FROM " + branchTable
                + " WHERE substatus NOT IN (SELECT sub_status FROM status) GROUP BY substatus";
        ResultSet resultSet = DbUtils.executeQueryWithReturn(query);
        if (resultSet.first()) {
            do {
                log.warn("Unknown status:" + resultSet.getString(1) + " (" + resultSet.getInt(2) + " occurences)");
            } while (resultSet.next());
            resultSet.next();
        }
        resultSet.close();
    }

    public static void dropTempTable(String branch) throws SQLException {
        log.info("Drop temp table");
        String branchTable = DbUtils.getResultTableForBranch(branch);
        DbUtils.executeQuery("DROP TABLE IF EXISTS " + branchTable);
    }

    public static void stats(String branch, String timestamp) throws SQLException {
        log.info("Calculating stats");
        String query = "UPDATE executions SET duration=UNIX_TIMESTAMP(end_date) - UNIX_TIMESTAMP(launch_date) WHERE duration IS NULL AND end_date IS NOT NULL";
        DbUtils.executeQuery(query);

        ResultSet resultSet;
        int idExec = DbUtils.getIdExec(branch, timestamp);

        query = "UPDATE executions SET nb_jobs=(SELECT COUNT(DISTINCT(id_job)) FROM results_def, jobs WHERE jobs.id=id_job AND jobs.id_validation=1 AND id_exec="
                + idExec + ") WHERE id=" + idExec;
        DbUtils.executeQuery(query);

        for (String language : new String[] { "java", "perl" }) {
            query = "SELECT COUNT(DISTINCT(id_job)) FROM results_def, jobs j WHERE id_exec=" + idExec + " AND language='"
                    + language + "' AND j.id=id_job AND id_validation=1";
            resultSet = DbUtils.executeQueryWithReturn(query);
            if (resultSet.first()) {
                int nbLanguage = resultSet.getInt(1);
                if (nbLanguage > 0) {
                    query = "INSERT INTO exec_stats VALUES (" + idExec + ", " + nbLanguage + ", '" + language
                            + "', NULL, NULL, NULL, NULL, NULL, NULL)";
                    DbUtils.executeQuery(query);

                    for (String status : new String[] { "Success", "Failure", "Error" }) {
                        query = "SELECT COUNT(DISTINCT(id_job)) FROM results_def, jobs j, status s WHERE id_exec=" + idExec
                                + " AND j.id=id_job AND id_status=s.id AND main_status='" + status + "' AND language='"
                                + language + "' AND id_validation=1";
                        ResultSet resultSet2 = DbUtils.executeQueryWithReturn(query);
                        if (resultSet2.first()) {
                            int nbLanguageStatus = resultSet2.getInt(1);

                            query = "UPDATE exec_stats SET " + status.toLowerCase() + "_nb=" + nbLanguageStatus + ", "
                                    + status.toLowerCase() + "_percent=" + ((float) nbLanguageStatus / nbLanguage)
                                    + " WHERE id_exec=" + idExec + " AND language='" + language + "'";
                            DbUtils.executeQuery(query);
                        }
                        resultSet2.close();
                    }
                }
            }
            resultSet.close();
        }
    }

}
