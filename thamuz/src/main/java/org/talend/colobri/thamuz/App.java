package org.talend.colobri.thamuz;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/**
 * Hello world!
 * 
 */
public class App extends Thread {

    private static Logger log = Logger.getLogger(App.class);

    AtomicBoolean canBeStopped = new AtomicBoolean(true);

    // only set to true is a stop is required and were are not in a long running task
    AtomicBoolean stopit = new AtomicBoolean(false);

    /**
     * DOC sgandon App constructor comment.
     * 
     * @param args
     */
    public App(String[] args) {
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        while (!stopit.get()) {
            // compute if TUJ launch is required
            TujLaunch tujLaunch = computeTujLaunchRequirement();
            canBeStopped.set(false);
            if (stopit.get()) {
                break;
            }
            // Launch the TUJ
            if (tujLaunch != null) {
                tujLaunch.perform();
            }// else not tuj to launch to loop
            canBeStopped.set(true);
            if (stopit.get()) {
                break;
            }
        }
    }

    /**
     * This mehtod will check if a TUJ launch is required and create a TujLaunch object if that is the case, it returns
     * null otherwise
     * 
     * @return a TujLaunch object if a launch is required or null if no launch is required
     */
    private TujLaunch computeTujLaunchRequirement() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * this should stop the server soon, will return only if this can be stopped smothly soon or need to be killed.
     */
    public void stopQuikly() {
        stopRequested();
    }

    /**
     * DOC sgandon Comment method "stopRequested".
     */
    synchronized private void stopRequested() {
        if (canBeStopped.get()) {
            stopit.set(true);
            try {
                this.join();
            } catch (InterruptedException e) {
                // if the thread is interrupted some how which should not be the case then logit;
                log.error("Stopping the Talend Deamon was interrupted but should not have been.", e); //$NON-NLS-1$
            }
        }

    }

}
