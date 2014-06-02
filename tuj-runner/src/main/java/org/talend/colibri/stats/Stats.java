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
package org.talend.colibri.stats;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import org.apache.log4j.Logger;
import org.talend.colibri.database.DbUtils;
import org.talend.colibri.properties.Bundles;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class Stats {

    private static Logger log = Logger.getLogger(Stats.class);

    private static class JobStat {

        String name;

        String language;

        long importInMillis;

        long executeInMillis;

        long totalInMillis;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.language == null) ? 0 : this.language.hashCode());
            result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            JobStat other = (JobStat) obj;
            if (this.language == null) {
                if (other.language != null)
                    return false;
            } else if (!this.language.equals(other.language))
                return false;
            if (this.name == null) {
                if (other.name != null)
                    return false;
            } else if (!this.name.equals(other.name))
                return false;
            return true;
        }

    }

    private static List<JobStat> list = new ArrayList<JobStat>();

    public static boolean storeJobstats() {
        try {
            String string = Bundles.getString("store.jobsStats");
            return new Boolean(string);
        } catch (MissingResourceException e) {
            return false;
        }
    }

    private static JobStat getJobstat(final String jobName, final String languageP) {
        JobStat searched = new JobStat();
        searched.name = jobName;
        searched.language = languageP;
        int indexOf = list.indexOf(searched);

        if (indexOf == -1) {
            JobStat toReturn = new JobStat();
            toReturn.name = jobName;
            toReturn.language = languageP;
            list.add(toReturn);
            return toReturn;
        } else {
            return list.get(indexOf);
        }
    }

    public static void registerImport(final String jobName, final String languageP, final long millisP) {
        if (!storeJobstats())
            return;

        getJobstat(jobName, languageP).importInMillis = millisP;
    }

    public static void registerExecution(final String jobName, final String languageP, final long millisP) {
        if (!storeJobstats())
            return;

        getJobstat(jobName, languageP).executeInMillis = millisP;
    }

    public static void registerTotal(final String jobName, final String languageP, final long millisP) {
        if (!storeJobstats())
            return;

        getJobstat(jobName, languageP).totalInMillis = millisP;
    }

    public static void persists(String branch, String timestamp) throws SQLException {
        if (!storeJobstats())
            return;

        log.info("Saving jobs duration");
        int idExec = DbUtils.getIdExec(branch, timestamp);
        for (JobStat current : list) {
            int idJob = DbUtils.getIdJob(current.language, current.name);
            String query = "INSERT INTO jobs_stats VALUES(" + idExec + ", " + idJob + ", " + current.totalInMillis / 1000 + ", "
                    + current.importInMillis / 1000 + ", " + current.executeInMillis / 1000 + ")";
            DbUtils.executeQuery(query);
        }
    }
}
