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
package org.talend.colibri.commandline;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jvnet.winp.WinProcess;
import org.talend.colibri.Misc;
import org.talend.colibri.execution.SocketHelp;
import org.talend.colibri.execution.log.MyLoggerFactory;
import org.talend.colibri.files.FilesUtils;
import org.talend.colibri.properties.Bundles;

public class CommandLine {

    private static Logger log = Logger.getLogger(CommandLine.class);

    private int port;

    private Thread outputReaderThread;

    private Thread errorReaderThread;

    private Logger innerLog;

    private Process process;

    public CommandLine(int port, String logFilePath) {
        super();
        this.port = port;

        // Instantiate file logger for this job execution:
        innerLog = Logger.getLogger(CommandLine.class + " - " + port, new MyLoggerFactory());
        try {
            FileAppender appender = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n"), logFilePath);
            innerLog.addAppender(appender);
        } catch (IOException e) {
            log.error(e);
        }
    }

    protected int timeout = Bundles.getInt("commandline.connection.timeout"); // In seconds

    private int waitTime = Bundles.getInt("commandline.connection.waitbetweentries"); // Wait between two calls to
                                                                                      // getCommandStatus, in
                                                                                      // milliseconds

    public void startCmdLine() throws CannotConnectCommandLineException {
        String path = Bundles.getString("commandline.path");
        String exec = Bundles.getString("commandline.execFile");

        log.info("Starting command line server on \"" + port + "\"");
        log.debug("    -> " + path);
        log.debug("    -> " + exec);

        String workspace = "workspace-" + port;
        File workspaceFile = new File(path + workspace);

        if (!new File(path + exec).exists()) {
            throw new CannotConnectCommandLineException("Cannot find file '" + path + exec + "'");
        }

        // Purge previous workspace:
        log.debug("Empty previous workspace " + workspace);
        FilesUtils.emptyFolder(workspaceFile);

        // TODO smallet copy components_workspace

        String command = path + exec + " -nosplash -application org.talend.commandline.CommandLine -consoleLog -data "
                + workspace + " startServer -p " + port;

        if (Misc.isWindows()) {
            command = "cmd /c " + command;
        }

        Runtime runtime = Runtime.getRuntime();
        try {
            process = runtime.exec(command, null, new java.io.File(path));
        } catch (IOException e) {
            throw new CannotConnectCommandLineException(e);
        }

        // Std output reader thread:
        outputReaderThread = new Thread() {

            public void run() {
                try {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(
                            process.getInputStream()));
                    String line = "";
                    try {
                        while ((line = reader.readLine()) != null) {
                            // System.out.println(line);
                            innerLog.trace(line);
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
        errorReaderThread = new Thread() {

            public void run() {
                try {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(
                            process.getErrorStream()));
                    String line = "";
                    try {
                        while ((line = reader.readLine()) != null) {
                            // System.err.println(line);
                            innerLog.warn(line);
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

        // wait for server to start
        int nbOccurences = timeout * 1000 / waitTime;
        boolean started = false;
        int n = 1;
        while (!started && n <= nbOccurences) {
            try {
                Socket socket = new Socket("localhost", port);
                started = true;
                socket.close();
            } catch (IOException e) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e1) {
                    log.trace("Interupted", e1);
                }
                log.trace("not started " + n);
                n++;
            }
        }
        if (started) {
            try {
                if (new SocketHelp().waitCommand(port, "initLocal"))
                    log.debug("Command line server listening on \"" + port + "\"");
                else
                    throw new CannotConnectCommandLineException("Unable to get initLocal socket response");
            } catch (BadCommandCommandLineException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new CannotConnectCommandLineException(e);
            } catch (TimeoutCommandLineException e) {
                throw new CannotConnectCommandLineException(e);
            }
        } else {
            throw new CannotConnectCommandLineException("Unable to connect to port " + port);
        }
    }

    public void stopCmdLine() throws IOException {
        log.info("Stopping command line server on \"" + port + "\"");

        outputReaderThread = null;
        errorReaderThread = null;

        new SocketHelp().sendSimpleCommand(port, "stopServer --force");

        boolean stopped = false;
        int nbOccurences = timeout * 1000 / waitTime;
        int n = 1;
        while (!stopped && n <= nbOccurences) {
            Socket socket = null;
            try {
                socket = new Socket("localhost", port);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e1) {
                }
                log.trace("not stopped " + n);
                n++;
            } catch (ConnectException e) {
                stopped = true;
            } finally {
                if (socket != null)
                    socket.close();
            }
        }

        if (!stopped && Misc.isWindows()) {
            log.debug("Not stopped => force stop");
            WinProcess wp = new WinProcess(process);
            wp.killRecursively();
        }
    }

    public List<String> listJob(String filter) throws IOException, BadCommandCommandLineException {
        SocketHelp socketHelp = new SocketHelp();

        String realFilter;
        if (filter != null && filter.length() > 0)
            realFilter = "((type=process)and(" + filter + "))";
        else
            realFilter = "(type=process)";

        String sendSimpleCommand2 = socketHelp.sendSimpleCommandWithReturn(port, "listItem --item-filter " + realFilter);

        List<String> toReturn = new ArrayList<String>();

        for (String cs : sendSimpleCommand2.split("\n")) {
            final String trim = cs.trim();
            if (!trim.startsWith("[")) {
                if (trim.contains(" v")) {
                    toReturn.add(trim.substring(0, trim.lastIndexOf(" v")));
                } else
                    toReturn.add(trim);
            }
        }
        return toReturn;
    }

}