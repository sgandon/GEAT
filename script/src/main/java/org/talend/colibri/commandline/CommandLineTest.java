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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.talend.colibri.Misc;
import org.talend.colibri.execution.SocketHelp;
import org.talend.colibri.properties.Bundles;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class CommandLineTest {

    private static Logger log = Logger.getLogger(CommandLineTest.class);

    public static void test(String branch, String timestamp) throws CannotConnectCommandLineException, IOException,
            BadCommandCommandLineException, TimeoutCommandLineException {
        log.info("Testing command line");
        String ports = Bundles.getString("commandline.ports");
        int port = Integer.parseInt(ports.split(",")[0]);

        String outputFolderPath = Misc.getOutputFolderPath(branch, timestamp) + "/logCommandLine#test";

        CommandLine commandLine = new CommandLine(port, outputFolderPath);
        commandLine.startCmdLine();

        // Test create and logon project:
        SocketHelp socketHelp = new SocketHelp();
        log.debug(port + "-Create a java project");
        String commandCreateProject = " createProject --project-name P_test --project-description test-auto"
                + " --project-language java --project-author tuj";
        socketHelp.waitCommand(port, commandCreateProject);
        String commandLog = "logonProject --project-name P_test --user-login tuj";
        socketHelp.waitCommand(port, commandLog);

        log.info("Command line ok");
        commandLine.stopCmdLine();
    }
}
