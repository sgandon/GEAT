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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.talend.colibri.Misc;
import org.talend.colibri.commandline.BadCommandCommandLineException;
import org.talend.colibri.commandline.CommandLine;
import org.talend.colibri.properties.Bundles;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class JobDispatcher {

    private static Logger log = Logger.getLogger(JobDispatcher.class);

    private ConcurrentLinkedQueue<LightJob> jobs = new ConcurrentLinkedQueue<LightJob>();

    public void dispatch(List<LightJob> jobs, String branch, String timestamp) {
        String ports = Bundles.getString("commandline.ports");
        log.info(jobs.size() + " jobs to launch on port: " + ports);

        String outputFolderPath = Misc.getOutputFolderPath(branch, timestamp);

        this.jobs.addAll(jobs);

        Set<Thread> threads = new HashSet<Thread>();

        for (String port : ports.split(",")) {
            Thread t = new JobDispatcherThread(Integer.parseInt(port), outputFolderPath + "/logCommandLine#" + port);
            threads.add(t);
            t.start();
        }

        while (getActiveCount(threads) != 0) {
            log.trace("  --> Nb command line running: " + getActiveCount(threads) + ", Jobs to do: " + this.jobs.size());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private int getActiveCount(Set<Thread> threads) {
        int toReturn = 0;
        for (Thread current : threads) {
            if (current.isAlive())
                toReturn++;
        }
        return toReturn;
    }

    private class JobDispatcherThread extends Thread {

        private int port;

        private CommandLine cmdLine;

        public JobDispatcherThread(int port, String logFile) {
            super(JobDispatcherThread.class.getSimpleName() + "-" + port);
            this.port = port;
            this.cmdLine = new CommandLine(port, logFile);
        }

        @Override
        public void run() {
            log.info(port + "-Starting thread");
            try {
                cmdLine.stopCmdLine();
            } catch (IOException e1) {
            }
            log.debug(port + "-Cmd line prevently stopped");
            try {
                log.debug(port + "-Cmd line starting");
                cmdLine.startCmdLine();
                log.debug(port + "-Cmd line started");
                SocketHelp socketHelp = new SocketHelp();

                log.debug(port + "-Create a java project");
                String commandCreateProject = " createProject --project-name P_java_" + port + " --project-description test-auto"
                        + " --project-language java --project-author tuj";
                socketHelp.waitCommand(port, commandCreateProject);

                log.debug(port + "-Create a perl project");
                commandCreateProject = " createProject --project-name P_perl_" + port + " --project-description test-auto"
                        + " --project-language perl --project-author tuj";
                socketHelp.waitCommand(port, commandCreateProject);

                String loggedOn = null;

                while (!jobs.isEmpty()) {
                    LightJob lightJob = jobs.poll();
                    if (lightJob != null) {
                        log.info("Job " + lightJob + " launched on port " + port + " (" + jobs.size() + " to run)");

                        if (loggedOn == null || !loggedOn.equals(lightJob.language)) {
                            String commandLog = "logonProject --project-name P_" + lightJob.language + "_" + port
                                    + " --user-login tuj";
                            socketHelp.waitCommand(port, commandLog);
                            loggedOn = lightJob.language;
                        }

                        Job job = new Job(lightJob.source, lightJob.branch, lightJob.language, lightJob.resultDestinationFolder
                                + lightJob.language + "/");
                        job.execute(port);
                    }
                }
                log.info(port + "-All jobs done !!");
            } catch (BadCommandCommandLineException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                try {
                    log.debug(port + "-Cmd line stopping");
                    cmdLine.stopCmdLine();
                    log.debug(port + "-Cmd line stopped");
                } catch (IOException e) {
                }
            }
        }
    }
}
