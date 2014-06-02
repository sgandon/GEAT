// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.colobri.thamuz;

import org.tanukisoftware.wrapper.WrapperListener;

/**
 * created by sgandon on 2 juin 2014 Detailled comment
 * 
 */
public class ServiceWrapperListener implements WrapperListener {

    App deamonApp;

    /*
     * (non-Javadoc)
     * 
     * @see org.tanukisoftware.wrapper.WrapperListener#controlEvent(int)
     */
    public void controlEvent(int arg0) {
        // TODO Auto-generated method stub

    }

    /**
     * The start method is called when the WrapperManager is signaled by the native Wrapper code that it can start its
     * application. This method call is expected to return, so a new thread should be launched if necessary.
     * 
     * @param args List of arguments used to initialize the application.
     * 
     * @return Any error code if the application should exit on completion of the start method. If there were no
     * problems then this method should return null.
     */
    public Integer start(String[] args) {
        deamonApp = new App(args);
        deamonApp.start();
        return null;
    }

    /**
     * Called when the application is shutting down. The Wrapper assumes that this method will return fairly quickly. If
     * the shutdown code code could potentially take a long time, then WrapperManager.signalStopping() should be called
     * to extend the timeout period. If for some reason, the stop method can not return, then it must call
     * WrapperManager.stopped() to avoid warning messages from the Wrapper.
     * 
     * @param exitCode The suggested exit code that will be returned to the OS when the JVM exits.
     * 
     * @return The exit code to actually return to the OS. In most cases, this should just be the value of exitCode,
     * however the user code has the option of changing the exit code if there are any problems during shutdown.
     */
    public int stop(int exitCode) {
        deamonApp.stopQuikly();
        return exitCode;
    }

}
