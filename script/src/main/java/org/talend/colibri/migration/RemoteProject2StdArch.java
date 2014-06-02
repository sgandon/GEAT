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

import org.talend.colibri.commandline.CommandLine;
import org.talend.colibri.execution.SocketHelp;
import org.talend.colibri.files.FilesUtils;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class RemoteProject2StdArch {

    public static void main(String[] args) {
        if (args.length != 6 && args.length != 7) {
            System.out.println("Migration from 'Remote TAC project' -> 'Standard tuj archive'");
            System.out.println("Parameters:");
            System.out.println("1. tis url            : url of tac");
            System.out.println("2. tis project        : name of project that contains tujs");
            System.out.println("3. tis username       : login of user to logon this project");
            System.out.println("4. tis password       : password of user to logon this project");
            System.out.println("5. files location path: path where files needed by tujs at execution are");
            System.out.println("6. destination path   : where to generate ourput files");
            System.out.println("7. jobs filter        : filter expression (see helpFilter in Command Line)");
            System.exit(0);
        }

        String filesPath = args[5 - 1]; // "/home/stephane/talend/automated_tests/migration/files/";
        String destinationPath = args[6 - 1]; // "/tmp/output/";

        String tisUrl = args[1 - 1]; // "http://dali:8080/tis300";
        boolean manageCommandLine = !tisUrl.equals("--");
        String tisProject = args[2 - 1]; // "TUJ";
        String tisUser = args[3 - 1]; // "smallet@talend.com";
        String tisPassword = args[4 - 1]; // "admin";

        String filter = (args.length == 7 ? args[7 - 1] : null);

        FilesUtils.emptyFolder(new File(destinationPath));

        final int port = 8002;
        CommandLine commandLine = new CommandLine(port, "/tmp/cmdlog/");

        try {
            SocketHelp socketHelp = new SocketHelp();

            if (manageCommandLine) {
                System.out.print(" - Starting Command Line on port " + port);
                commandLine.startCmdLine();
                System.out.println("  -> Ok");

                String initCommand = "initRemote " + tisUrl;
                System.out.print(" - Waiting for command '" + initCommand + "'");
                if (!socketHelp.waitCommand(port, initCommand)) {
                    throw new RuntimeException("Cannot initRemote on '" + tisUrl + "'");
                }
                System.out.println("  -> Ok");

                String commandLog = "logonProject --project-name " + tisProject + " --user-login " + tisUser
                        + " --user-password " + tisPassword;
                System.out.print(" - Waiting for command '" + commandLog + "'");
                if (!socketHelp.waitCommand(port, commandLog)) {
                    throw new RuntimeException("Cannot logon on '" + tisProject + "' with username='" + tisUser
                            + "' and password='" + tisPassword + "'");
                }

                System.out.println("  -> Ok");
            }

            List<String> listJob = commandLine.listJob(filter);

            for (String currentJob : listJob) {
                System.out.println(" - Copying job: " + currentJob);
                File dest = new File(destinationPath + currentJob);
                dest.mkdir();

                // Jobs:
                String exportItemCommand = "exportItems " + dest.getAbsolutePath().replace("\\", "/")
                        + " --dependencies --item-filter ((type=process)and(label=" + currentJob + "))";
                if (!socketHelp.waitCommand(port, exportItemCommand)) {
                    throw new RuntimeException("Error while processing command '" + exportItemCommand + "'");
                }

                // Files:
                File sourceFiles = new File(filesPath + currentJob);
                if (sourceFiles.exists()) {
                    FilesUtils.copyFolder(sourceFiles, dest);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (manageCommandLine) {
                try {
                    commandLine.stopCmdLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
