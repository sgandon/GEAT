// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.colibri.commandline.BadCommandCommandLineException;
import org.talend.colibri.commandline.CannotConnectCommandLineException;
import org.talend.colibri.commandline.CommandLine;
import org.talend.colibri.commandline.TimeoutCommandLineException;

public class SocketHelpTest {

    private static final int CMD_LINE_PORT = 8002;

    private static CommandLine cmdLine = new CommandLine(CMD_LINE_PORT, "/tmp/tuj-junit.log");

    @BeforeClass
    public static void setUp() throws CannotConnectCommandLineException {
        cmdLine.startCmdLine();
    }

    @AfterClass
    public static void after() throws IOException {
        cmdLine.stopCmdLine();
    }

    // @Test(expected = TimeoutCommandLineException.class)
    public void testTimeout() throws IOException, TimeoutCommandLineException, BadCommandCommandLineException {
        SocketHelp socketHelp = new SocketHelp();

        socketHelp
                .waitCommand(
                        8002,
                        "createProject --project-name TOTO --project-description desc --project-language java --project-author smallet@talend.com --project-author-password admin");

        socketHelp.timeout = 1;
        socketHelp.waitCommand(8002, "logonProject --project-name TOTO --user-login smallet@talend.com --user-password admin");
    }

    // @Test
    public void testNoTimeout() throws IOException, TimeoutCommandLineException, BadCommandCommandLineException {
        SocketHelp socketHelp = new SocketHelp();

        socketHelp
                .waitCommand(
                        8002,
                        "createProject --project-name TOTO --project-description desc --project-language java --project-author smallet@talend.com --project-author-password admin");

        socketHelp.waitCommand(8002, "logonProject --project-name TOTO --user-login smallet@talend.com --user-password admin");
    }

    @Test
    public void testWait5Minutes() {
        try {
            Thread.sleep(5 * 60 * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
