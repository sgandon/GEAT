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
package org.talend.colibri.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.talend.colibri.EntryPoint;
import org.talend.colibri.Misc;
import org.talend.colibri.execution.log.MyLoggerFactory;
import org.talend.colibri.files.ResourcesUtils;
import org.talend.colibri.files.ZipUtils;
import org.talend.colibri.properties.Bundles;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class Scripts {

    private static Logger log = Logger.getLogger(Scripts.class);

    public static void main(String[] args) {
        try {
            Bundles.init(args[1]);
            launch(args[0], Integer.parseInt(args[2]), args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void launch(String fileName, int revision, String branch) throws IOException, InterruptedException,
            ScriptFailedException {
        String str;
        BufferedReader in = new BufferedReader(new FileReader(ResourcesUtils.getResource(fileName)));
        try {
            while ((str = in.readLine()) != null) {
                if (!str.startsWith("#") && str.length() > 0) {
                    String[] split = str.split(",", 2);
                    String scriptName = split[0];

                    String scriptCommand = replaceParamInScript(split[1], revision, branch);

                    executeLine(fileName, scriptName, scriptCommand, branch, revision);
                }
            }
        } finally {
            in.close();
        }
    }

    private static void executeLine(String fileName, String scriptName, String scriptCommand, String branch, int revision)
            throws IOException, InterruptedException, ScriptFailedException {
        if (scriptCommand == null || scriptCommand.length() == 0)
            return;

        long start = System.currentTimeMillis();
        log.info("Executing script '" + scriptName + "': [" + scriptCommand + "]");

        try {
            EntryPoint.updateExecution(branch, revision, "Running " + fileName + " " + scriptName, false);
        } catch (SQLException e) {
            log.warn(e);
        }

        final String logFileName = Misc.getOutputFolderPath(branch, revision) + scriptName + ".log";

        final Logger innerLogger = Logger.getLogger(scriptName, new MyLoggerFactory());
        FileAppender appender = null;
        try {
            appender = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n"), logFileName);
            innerLogger.addAppender(appender);
        } catch (IOException e) {
            log.error(e);
        }

        Process process;
        Runtime runtime = Runtime.getRuntime();

        String[] cmdarray;
        if (Misc.isWindows()) {
            cmdarray = new String[] { "cmd", "/c", scriptCommand };
        } else {
            cmdarray = new String[] { "/bin/sh", "-c", scriptCommand };
        }
        process = runtime.exec(cmdarray);

        final InputStream inputStream = process.getInputStream();
        final InputStream errorStream = process.getErrorStream();

        // Std output reader thread:
        Thread outputReaderThread = new Thread() {

            public void run() {
                try {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                    String line = "";
                    try {
                        while ((line = reader.readLine()) != null) {
                            // System.out.println(line);
                            innerLogger.info(line);
                        }
                    } finally {
                        reader.close();
                    }
                } catch (java.io.IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        };
        outputReaderThread.start();

        // Err output reader thread:
        Thread errorReaderThread = new Thread() {

            public void run() {
                try {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(errorStream));
                    String line = "";
                    try {
                        while ((line = reader.readLine()) != null) {
                            // System.err.println(line);
                            innerLogger.warn(line);
                        }
                    } finally {
                        reader.close();
                    }
                } catch (java.io.IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        };
        errorReaderThread.start();

        process.waitFor();

        if (appender != null) {
            appender.close();
            innerLogger.removeAppender(appender);
        }

        int exitValue = process.exitValue();
        long end = System.currentTimeMillis();
        if (exitValue == 0) {
            log.info("Script success in " + (end - start) / 1000 + " seconds");
        } else {
            log.info("Script failed in " + (end - start) / 1000 + " seconds, exit value=" + exitValue);
            ZipUtils.zip(logFileName, logFileName + ".zip");
            throw new ScriptFailedException(scriptName);
        }
        new File(logFileName).delete();
    }

    private static String replaceParamInScript(String script, int revision, String branch) {
        script = script.replaceAll("\\{revision\\}", "" + revision);
        script = script.replaceAll("\\{branch\\}", "" + branch);
        script = script.replaceAll("\\{outputFolder\\}", "" + Misc.getOutputFolderPath(branch, revision));

        String sequence = "--";
        while (sequence != null) {
            sequence = searchParam(script);
            if (sequence != null) {
                script = script.replaceAll("\\{" + sequence + "\\}", Bundles.getString(sequence));
            }
        }

        return script;
    }

    private static String searchParam(String input) {
        int indexOf = input.indexOf("{");
        if (indexOf != -1) {
            int indexOf2 = input.indexOf("}");
            if (indexOf2 == -1)
                throw new IllegalArgumentException();
            return input.substring(indexOf + 1, indexOf2);
        } else {
            return null;
        }
    }

}
