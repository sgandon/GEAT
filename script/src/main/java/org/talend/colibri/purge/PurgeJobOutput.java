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
package org.talend.colibri.purge;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.talend.colibri.Misc;
import org.talend.colibri.database.DbUtils;
import org.talend.colibri.files.FilesUtils;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class PurgeJobOutput {

    private static Logger log = Logger.getLogger(PurgeJobOutput.class);

    public static void main(String[] args) throws SQLException {
        purgeJobsOk("branches/branch-3_2", 32763);
    }

    /**
     * Delete output folders for jobs with 'Ok' status. We remove these folders to gain space on disk. We assume that we
     * don't need log files in case of success.
     */
    public static void purgeJobsOk(String branch, int revision) throws SQLException {
        log.info("Purge folder (remove output folder for jobs ok)");
        String outputFolderPath = Misc.getOutputFolderPath(branch, revision);
        int idExec = DbUtils.getIdExec(branch, revision);
        String query = "SELECT DISTINCT(id_job),  name, language FROM results_def, jobs WHERE jobs.id=id_job AND id_exec="
                + idExec + " AND id_status=1 AND id_job NOT IN " + "(SELECT DISTINCT(id_job) FROM results_def WHERE id_exec="
                + idExec + " AND id_status!=1)";
        ResultSet resultSet = DbUtils.executeQueryWithReturn(query);
        boolean hasNext = resultSet.first();
        while (hasNext) {
            String name = resultSet.getString("name");
            String language = resultSet.getString("language");

            final File toEmpty = new File(outputFolderPath + language + "/" + name);
            FilesUtils.emptyFolder(toEmpty);
            toEmpty.delete();

            hasNext = resultSet.next();
        }
        resultSet.close();
    }
}
