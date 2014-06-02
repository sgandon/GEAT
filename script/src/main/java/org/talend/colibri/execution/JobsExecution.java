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
package org.talend.colibri.execution;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;

import org.apache.log4j.Logger;
import org.talend.colibri.Misc;
import org.talend.colibri.database.TablesCreation;
import org.talend.colibri.properties.Bundles;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class JobsExecution {

    private static Logger log = Logger.getLogger(JobsExecution.class);

    public static void main(String[] args) throws SQLException {
        final String branch2 = "branches/branch-3_2";
        TablesCreation.tempResultTableCreation(branch2);
        new JobsExecution().execute(branch2, 32761);
    }

    public static void execute(String branch, int revision) {
        String outputFolder = Misc.getOutputFolderPath(branch, revision);

        List<LightJob> jobs = new ArrayList<LightJob>();
        jobs.addAll(addJobs(branch, outputFolder, "java"));
        jobs.addAll(addJobs(branch, outputFolder, "perl"));

        new JobDispatcher().dispatch(jobs, branch, revision);
    }

    private static List<LightJob> addJobs(String branch, String outputFolder, String language) {
        List<LightJob> toReturn = new ArrayList<LightJob>();
        try {
            String sources = Bundles.getString("jobs.source." + language);
            for (String current : sources.split(",")) {
                final File currentSourceFolder = new File(current);
                log.debug("Scanning source folder '" + currentSourceFolder.getAbsolutePath() + "'");
                if (currentSourceFolder.exists()) {
                    final List<File> sourceFolders = Arrays.asList(currentSourceFolder.listFiles(new SvnFilter()));
                    log.trace(" -> source folder contains " + sources.length() + " sub folders");
                    for (File sourceFolder : sourceFolders) {
                        LightJob lightJob = new LightJob();
                        lightJob.language = language;
                        lightJob.branch = branch;
                        lightJob.source = sourceFolder;
                        lightJob.resultDestinationFolder = outputFolder;
                        toReturn.add(lightJob);
                    }
                } else {
                    log.trace(" -> source folder doesn't exist");
                }
            }
        } catch (MissingResourceException e) {
        }
        return toReturn;
    }

    private static class SvnFilter implements FilenameFilter {

        public boolean accept(File arg0, String arg1) {
            return !arg1.equals(".svn");
        }

    }
}
