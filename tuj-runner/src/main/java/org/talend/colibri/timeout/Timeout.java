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
package org.talend.colibri.timeout;

import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.talend.colibri.commandline.CommandLine;

/**
 * DOC stephane class global comment. Detailled comment <br/>
 */
public abstract class Timeout {

    private static Logger log = Logger.getLogger(CommandLine.class);

    protected int nbTries = 10;

    private int waitTime = 500;

    // Wait between two calls to getCommandStatus, in milliseconds

    public abstract boolean isFinished();

    public abstract void processStep() throws Exception;

    public void run() throws TimeoutException, Exception {
        for (int i = 1; i <= nbTries; i++) {
            log.trace("Try #" + i);
            processStep();
            if (isFinished()) {
                return;
            } else {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                }
            }
        }
        throw new TimeoutException("Delay of " + (nbTries * waitTime) + " ms");
    }

}
