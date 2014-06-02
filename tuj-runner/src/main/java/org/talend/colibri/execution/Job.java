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
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.talend.colibri.Misc;
import org.talend.colibri.commandline.BadCommandCommandLineException;
import org.talend.colibri.commandline.TimeoutCommandLineException;
import org.talend.colibri.database.DateFormater;
import org.talend.colibri.database.DbUtils;
import org.talend.colibri.execution.log.MyLoggerFactory;
import org.talend.colibri.files.FilesUtils;
import org.talend.colibri.files.ResourcesUtils;
import org.talend.colibri.properties.Bundles;
import org.talend.colibri.stats.Stats;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class Job {

    private static Logger log = Logger.getLogger(Job.class);

    private String jobName;

    private File source;

    private String branch;

    private String language;

    private File resultDestinationFolder;

    private Logger innerLogger;

    public Job(File source, String branch, String language, String resultDestinationFolder) {
        super();
        this.jobName = source.getName();
        this.source = source;
        this.branch = branch;
        this.language = language;

        this.resultDestinationFolder = new File(resultDestinationFolder + jobName);
    }

    public void execute(int cmdLinePort) throws SQLException, IOException, BadCommandCommandLineException {
        long start = System.currentTimeMillis();
        // Create output directory:
        if (this.resultDestinationFolder.exists())
            FilesUtils.emptyFolder(this.resultDestinationFolder);
        else
            this.resultDestinationFolder.mkdirs();

        // Instantiate file logger for this job execution:
        innerLogger = Logger.getLogger(jobName, new MyLoggerFactory());
        FileAppender appender = null;
        try {
            appender = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n"),
                    this.resultDestinationFolder.getAbsolutePath() + "/execLog");
            innerLogger.addAppender(appender);
        } catch (IOException e) {
            log.error(e);
        }

        innerLogger.info("Execute job '" + jobName + "' on command line " + cmdLinePort);
        DbUtils.executeQuery("INSERT INTO port_activity VALUES (" + cmdLinePort + ", '" + branch + "', '" + jobName + "', '"
                + language + "', '" + DateFormater.getInstance().format(new Date()) + "')");

        try {
            createDestinationFolder();
            run(cmdLinePort);
            resultAnalysis();
        } catch (TimeoutCommandLineException e) {
            insertIntoTempResult("Timeout", e.getMessage());
        } catch (IOException e) {
            innerLogger.error(e.getMessage(), e);
            log.error(e);
            insertIntoTempResult("Cannot connect to command line", "Cannot connect to command line");
        } catch (BadCommandCommandLineException e) {
            innerLogger.error(e.getMessage(), e);
            log.error(e);
            insertIntoTempResult("Cannot connect to command line", "Bad syntax in command line " + e.getMessage());
        } finally {
            DbUtils.executeQuery("DELETE FROM port_activity WHERE port=" + cmdLinePort);
            long end = System.currentTimeMillis();
            Stats.registerTotal(jobName, language, end - start);
        }

        if (appender != null) {
            appender.close();
            innerLogger.removeAppender(appender);
        }
    }

    private void run(int cmdLinePort) throws IOException, BadCommandCommandLineException, TimeoutCommandLineException {
        String commandImport = "importItems " + source.getAbsolutePath() + " --overwrite";
        if (Misc.isWindows())
            commandImport = commandImport.replace("\\", "\\\\");

        StringBuffer commandExecute = new StringBuffer("executeJob " + jobName);
        commandExecute.append(" --interpreter " + Bundles.getString("jobs.interpreter." + language));
        commandExecute.append(" --job-result-destination-dir " + resultDestinationFolder.getAbsolutePath() + "/log");
        commandExecute.append(" --job-timeout " + Bundles.getString("jobs.execution.timeout"));

        commandExecute.append(" --job-context-param");
        commandExecute.append(" param_file_path=" + ResourcesUtils.getResource("template.csv").getAbsolutePath());
        commandExecute.append(" result_table=" + DbUtils.getResultTableForBranch(branch));
        commandExecute.append(" data_dir=" + source.getParentFile().getAbsolutePath());
        commandExecute.append(" data_output_dir=" + resultDestinationFolder.getAbsolutePath() + "/data/");

        String sCommandExecute = commandExecute.toString();
        if (Misc.isWindows())
            sCommandExecute = sCommandExecute.replace("\\", "/");

        SocketHelp socketHelp = new SocketHelp(innerLogger);

        long start = System.currentTimeMillis();
        boolean imported = socketHelp.waitCommand(cmdLinePort, commandImport);
        long end = System.currentTimeMillis();
        Stats.registerImport(jobName, language, end - start);
        if (imported) {
            start = System.currentTimeMillis();
            boolean executed = socketHelp.waitCommand(cmdLinePort, sCommandExecute);
            end = System.currentTimeMillis();
            Stats.registerExecution(jobName, language, end - start);
            if (!executed) {
                innerLogger.warn("command execute return false");
            }
        } else {
            innerLogger.warn("command import return false");
        }
    }

    private void createDestinationFolder() {
        innerLogger.trace("Creating output folders");

        new File(resultDestinationFolder.getAbsolutePath() + "/log").mkdirs();
        new File(resultDestinationFolder.getAbsolutePath() + "/data").mkdirs();
    }

    private void resultAnalysis() throws SQLException, IOException {
        // Get 'state' file:
        File state = new File(resultDestinationFolder.getAbsolutePath() + "/log/state");

        // Case of 'state' file is not generated:
        if (!state.exists()) {
            insertIntoTempResult("Didn\\'t run", "No \\'state\\' file");
        } else {
            String stateContent = FilesUtils.getFirstString(state);
            if (stateContent.equals("FAILED")) {
                insertIntoTempResult("Failed to generate code", "Failed to generate code");
            } else if (stateContent.equals("COMPLETED")) {
                ResultSet resultSet = DbUtils.executeQueryWithReturn("SELECT count(*) FROM "
                        + DbUtils.getResultTableForBranch(branch) + " WHERE job='" + jobName + "'");
                int nbLinesGeneratedByJob = 0;
                if (resultSet.first())
                    nbLinesGeneratedByJob = resultSet.getInt("count(*)");
                resultSet.close();

                if (nbLinesGeneratedByJob == 0) {
                    // Get 'error' file:
                    File errorFile = new File(resultDestinationFolder.getAbsolutePath() + "/log/" + jobName + "_stderr");

                    if (errorFile.exists()) {
                        insertIntoTempResult(getSubstatus(FilesUtils.getFirstString(errorFile)), null);
                    } else {
                        File exitCodeFile = new File(resultDestinationFolder.getAbsolutePath() + "/log/" + jobName + "_exit");
                        int exitCode = Integer.parseInt(FilesUtils.getFirstString(exitCodeFile));
                        String subStatus = (exitCode == 0 ? "No assert logged/no error file" : "Error code=" + exitCode);

                        insertIntoTempResult(subStatus, "Error code=" + exitCode);
                    }
                }
            }
        }
    }

    private void insertIntoTempResult(String subStatus, String description) throws SQLException {
        if (description == null)
            description = "NULL";
        else
            description = "'" + description + "'";
        DbUtils.executeQuery("INSERT INTO " + DbUtils.getResultTableForBranch(branch) + " VALUES('"
                + DateFormater.getInstance().format(new Date()) + "', NULL, NULL, '" + jobName + "', '" + language
                + "', NULL, 'Failed', '" + subStatus + "', " + description + ")");
    }

    private static String getSubstatus(String stderr) {
        if (stderr.contains("java.lang.Error") && stderr.contains("compilation")) {
            return "Unresolved compilation problem";
        }
        if (stderr.matches("^(Can\'t locate).*(\\.pm).*")) {
            return "Missing module";
        }
        return "Unknown";
    }

}
