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
package org.talend.colibri;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.talend.colibri.archive.ArchiveResults;
import org.talend.colibri.commandline.BadCommandCommandLineException;
import org.talend.colibri.commandline.CannotConnectCommandLineException;
import org.talend.colibri.commandline.CommandLineTest;
import org.talend.colibri.commandline.TimeoutCommandLineException;
import org.talend.colibri.database.DateFormater;
import org.talend.colibri.database.DbUtils;
import org.talend.colibri.database.PurgeDatabase;
import org.talend.colibri.database.TablesCreation;
import org.talend.colibri.execution.JobsExecution;
import org.talend.colibri.files.FilesUtils;
import org.talend.colibri.properties.Bundles;
import org.talend.colibri.purge.PurgeJobOutput;
import org.talend.colibri.scripts.ScriptFailedException;
import org.talend.colibri.scripts.Scripts;
import org.talend.colibri.stats.Stats;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class EntryPoint {

    private static Logger log = Logger.getLogger(EntryPoint.class);

    public static void main(String[] args) throws SQLException {
        final int nbArgs = 2;
        if (args.length != nbArgs)
            throw new IllegalArgumentException("Waiting " + nbArgs + " arguments [branch revision], receiving "
                    + Arrays.toString(args));
        String branch = args[0];
        int revision = Integer.parseInt(args[1]);

        try {
            // Purge file system:
            File outputFolder = new File(Misc.getOutputFolderPath(branch, revision));
            if (outputFolder.exists()) {
                FilesUtils.emptyFolder(outputFolder);
            } else {
                outputFolder.mkdirs();
            }

            // Init log4j file appender:
            FileAppender appender = (FileAppender) Logger.getRootLogger().getAppender("MyFileAppender");
            if (appender != null) {
                final String file = outputFolder.getAbsolutePath() + "/exec.log";
                appender.setFile(file);
                appender.activateOptions();
            }
        } catch (Exception e) {
            log.fatal(e.getMessage(), e);
            System.exit(1);
        }

        log.info("Starting execution");
        log.info("Parameters:");
        log.info(" -> branch=" + branch);
        log.info(" -> revision=" + revision);

        Bundles.init(branch);

        DbUtils.testConnection();

        TablesCreation.modelTablesCreation();

        PurgeDatabase.purge(branch, revision);

        DbUtils.executeQuery("DELETE FROM executions WHERE branch='" + branch + "' AND revision=" + revision);

        DbUtils.executeQuery("INSERT INTO executions SET branch='" + branch + "', revision=" + revision + ", launch_date='"
                + DateFormater.getInstance().format(new Date()) + "', status='Launched', log_files='"
                + Misc.getOutputFolderPath(branch, revision) + "'");

        try {
            Scripts.launch("preScripts", revision, branch);

            CommandLineTest.test(branch, revision);

            updateExecution(branch, revision, "Build ok", false);

            TablesCreation.tempResultTableCreation(branch);

            JobsExecution.execute(branch, revision);

            ArchiveResults.archive(branch, revision);

            // Alertes mail

            // Suppression des workspace command line
            // -> dans la thread

            Stats.persists(branch, revision);

            // Analyse des composants

            PurgeJobOutput.purgeJobsOk(branch, revision);

            Scripts.launch("postScripts", revision, branch);
            updateExecution(branch, revision, "Done", true);
        } catch (CannotConnectCommandLineException e) {
            log.error(e.getMessage(), e);
            updateExecution(branch, revision, "Build failed", true);
        } catch (TimeoutCommandLineException e) {
            log.error(e.getMessage(), e);
            updateExecution(branch, revision, "Build failed", true);
        } catch (BadCommandCommandLineException e) {
            log.error(e.getMessage(), e);
            updateExecution(branch, revision, "Build failed", true);
        } catch (ScriptFailedException e) {
            updateExecution(branch, revision, e.getScriptName() + " failed", true);
        } catch (Exception e) {
            updateExecution(branch, revision, "Error", true);
            log.error(e.getMessage(), e);
        } finally {
            ArchiveResults.dropTempTable(branch, revision);
            ArchiveResults.stats(branch, revision);
        }
        log.info("Execution finished");
    }

    public static void updateExecution(String branch, int revision, String status, boolean setEndDate) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE executions SET status='" + status + "'");
        if (setEndDate)
            sb.append(", end_date='" + DateFormater.getInstance().format(new Date()) + "'");
        sb.append(" WHERE branch='" + branch + "' AND revision=" + revision);
        DbUtils.executeQuery(sb.toString());
    }
}
