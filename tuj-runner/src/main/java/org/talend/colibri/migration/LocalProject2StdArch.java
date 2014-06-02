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
package org.talend.colibri.migration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.talend.colibri.commandline.BadCommandCommandLineException;
import org.talend.colibri.commandline.CannotConnectCommandLineException;
import org.talend.colibri.commandline.CommandLine;
import org.talend.colibri.commandline.TimeoutCommandLineException;
import org.talend.colibri.execution.SocketHelp;
import org.talend.colibri.files.FilesUtils;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class LocalProject2StdArch {

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Migration from 'Local TAC project' -> 'Standard tuj archive'");
            System.out.println("Parameters:");
            System.out.println("1. project path       : path to project");
            System.out.println("2. project language   : java/perl");
            System.out.println("3. files location path: path where files needed by tujs at execution are");
            System.out.println("4. destination path   : where to generate output files");
            System.exit(0);
        }

        String projectPath = args[1 - 1]; // "http://dali:8080/tis300";
        String language = args[2 - 1]; // "java";
        String filesPath = args[3 - 1]; // "/home/stephane/talend/automated_tests/migration/files/";
        String destinationPath = args[4 - 1]; // "/tmp/output/";

        FilesUtils.emptyFolder(new File(destinationPath));

        final int port = 8002;
        CommandLine commandLine = new CommandLine(port, "/tmp/cmdlog/");

        try {
            commandLine.startCmdLine();
            SocketHelp socketHelp = new SocketHelp();

            String initCommand = "initLocal";
            if (!socketHelp.waitCommand(port, initCommand)) {
                throw new RuntimeException("Cannot initLocal");
            }

            String projectName = LocalProject2StdArch.class.getSimpleName() + "_" + System.currentTimeMillis();

            String createProjectCommand = "createProject --project-name " + projectName + " --project-description "
                    + LocalProject2StdArch.class.getSimpleName() + "-tempProject --project-language " + language
                    + " --project-author test@talend.com";
            if (!socketHelp.waitCommand(port, createProjectCommand)) {
                throw new RuntimeException("Cannot create project");
            }

            String commandLog = "logonProject --project-name " + projectName
                    + " --user-login test@talend.com --user-password pwd";
            if (!socketHelp.waitCommand(port, commandLog)) {
                throw new RuntimeException("Cannot logon on '" + projectName + "'");
            }

            String commandImport = "importItems " + projectPath;
            if (!socketHelp.waitCommand(port, commandImport)) {
                throw new RuntimeException("Cannot import items");
            }

            List<String> listJob = commandLine.listJob(null);

            if (!listJob.isEmpty())
                for (String currentJob : listJob) {
                    File dest = new File(destinationPath + currentJob);
                    dest.mkdir();

                    // Jobs:
                    String exportItemCommand = "exportItems " + dest.getAbsolutePath()
                            + " --item-filter ((type=process)and(label=" + currentJob + "))";
                    if (!socketHelp.waitCommand(port, exportItemCommand)) {
                        throw new RuntimeException("Error while processing command '" + exportItemCommand + "'");
                    }

                    // Files:
                    File sourceFiles = new File(filesPath + currentJob);
                    if (sourceFiles.exists()) {
                        FilesUtils.copyFolder(sourceFiles, dest);
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadCommandCommandLineException e) {
            e.printStackTrace();
        } catch (CannotConnectCommandLineException e) {
            e.printStackTrace();
        } catch (TimeoutCommandLineException e) {
            e.printStackTrace();
        } finally {
            try {
                commandLine.stopCmdLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
