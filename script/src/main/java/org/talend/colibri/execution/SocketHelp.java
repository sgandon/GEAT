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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.talend.colibri.commandline.BadCommandCommandLineException;
import org.talend.colibri.commandline.TimeoutCommandLineException;
import org.talend.colibri.properties.Bundles;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class SocketHelp {

    private static final String CMD_LINE_PROMPT = "talend> ";

    private Logger log;

    public SocketHelp() {
        log = Logger.getLogger(SocketHelp.class);
    }

    public SocketHelp(Logger logger) {
        this.log = logger;
    }

    public void sendSimpleCommand(int port, String command) throws IOException {
        Socket socket = null;
        PrintWriter printWriter = null;
        try {
            // socket = new Socket("10.42.20.149", port);
            socket = new Socket("localhost", port);
            printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(command);
            printWriter.flush();
        } finally {
            try {
                printWriter.close();
            } catch (Exception e) {
            }
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }

    public String sendSimpleCommandWithReturn(int port, String command) throws IOException, BadCommandCommandLineException {
        log.debug("Sending command with return '" + command + "' on port " + port);
        Socket socket = null;
        PrintWriter printWriter = null;
        BufferedReader reader = null;
        try {
            socket = new Socket("localhost", port);
            printWriter = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter.println(command);
            printWriter.flush();

            StringBuffer toReturn = new StringBuffer();

            String response = "";
            while ((response = readLine(reader)) != null) {
                if (response.contains("java.lang.IllegalStateException")) {
                    throw new BadCommandCommandLineException(response);
                }
                if (response.startsWith(CMD_LINE_PROMPT)) {
                    String substring = response.substring(CMD_LINE_PROMPT.length());
                    if (substring.startsWith(CMD_LINE_PROMPT)) {
                        substring = substring.substring(CMD_LINE_PROMPT.length());
                    }
                    toReturn.append(substring + "\n");
                } else {
                    toReturn.append(response + "\n");
                }
            }
            return toReturn.toString();
        } finally {
            try {
                printWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String readLine(final BufferedReader reader) {
        final String[] toReturn = new String[] { null };
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    toReturn[0] = reader.readLine();
                } catch (IOException e) {
                    // throw new RuntimeException(e);
                }
            }

        };
        t.start();

        try {
            t.join(4000);
        } catch (InterruptedException e) {
        }
        return toReturn[0];
    }

    public boolean waitCommand(int port, String command) throws IOException, BadCommandCommandLineException,
            TimeoutCommandLineException {
        int id = sendCommand(port, command);
        return waitCommand(port, id);
    }

    private int sendCommand(int port, String command) throws IOException, BadCommandCommandLineException {
        log.debug("Sending command '" + command + "' on port " + port);
        Socket socket = null;
        PrintWriter printWriter = null;
        BufferedReader reader = null;
        try {
            socket = new Socket("localhost", port);
            printWriter = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            printWriter.println(command);
            printWriter.flush();

            String returnCmd = null;
            int t = -1;
            while (t < 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                }
                returnCmd = reader.readLine();
                if (returnCmd != null) {
                    if (returnCmd.matches("^\\|Unexpected .+ while processing .*")) {
                        throw new BadCommandCommandLineException(returnCmd);
                    }
                    t = returnCmd.indexOf("ADDED_COMMAND");
                }
            }
            int id = Integer.parseInt(returnCmd.substring(t + 14));
            log.debug("id command=" + id);
            return id;
        } finally {
            try {
                printWriter.close();
            } catch (Exception e) {
            }
            try {
                reader.close();
            } catch (Exception e) {
            }
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }

    protected int timeout = Bundles.getInt("jobs.execution.timeout"); // In seconds

    private int waitTime = Bundles.getInt("commandline.connection.waitbetweentries"); // Wait between two calls to
                                                                                      // getCommandStatus, in
                                                                                      // milliseconds

    protected boolean waitCommand(int port, int commandId) throws IOException, TimeoutCommandLineException {

        int nbOccurences = timeout * 1000 / waitTime;

        log.trace("Timeout config: " + timeout + " sec (" + waitTime + " ms X " + nbOccurences + ")");

        Socket socket = null;
        PrintWriter printWriter = null;
        BufferedReader reader = null;
        try {
            String response = "RUNNING";
            int currentOccurence = 0;
            while ((response.contains("RUNNING") || response.contains("WAITING")) && (currentOccurence < nbOccurences)) {
                socket = new Socket("localhost", port);
                printWriter = new PrintWriter(socket.getOutputStream());
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                printWriter.println("getCommandStatus " + commandId);
                printWriter.flush();
                try {
                    response = reader.readLine();
                } catch (IOException e) {
                    log.error(e);
                    throw e;
                }

                try {
                    printWriter.close();
                } catch (Exception e) {
                }
                try {
                    reader.close();
                } catch (Exception e) {
                }
                try {
                    socket.close();
                } catch (Exception e) {
                }

                if (response.contains("RUNNING") || response.contains("WAITING")) {
                    try {
                        Thread.sleep(waitTime);
                        currentOccurence++;
                    } catch (InterruptedException e) {
                    }
                } else if (response.contains("COMPLETED")) {
                    log.debug(response);
                } else if (response.contains("FAILED")) {
                    logGetCommandStatus(commandId, response, port);
                }
            }

            if (nbOccurences == currentOccurence)
                throw new TimeoutCommandLineException(port, commandId, timeout);

            return response.contains("COMPLETED");
        } finally {
            try {
                printWriter.close();
            } catch (Exception e) {
            }
            try {
                reader.close();
            } catch (Exception e) {
            }
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }

    private void logGetCommandStatus(int commandId, String response, int port) throws IOException {
        Socket socket = null;
        PrintWriter printWriter = null;
        BufferedReader reader = null;

        try {
            socket = new Socket("localhost", port);
            printWriter = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter.println("getCommandStatus " + commandId);
            printWriter.flush();
            try {
                while ((response = reader.readLine()) != null && response.trim().length() > 0)
                    log.debug(response);
            } catch (IOException e) {
                log.error(e);
                throw e;
            }
        } finally {
            try {
                printWriter.close();
            } catch (Exception e) {
            }
            try {
                reader.close();
            } catch (Exception e) {
            }
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }
}
