package org.talend.geat.exception;

/**
 * If a required remote item (repository or branch) does not exist.
 */
public class NotRemoteException extends Exception {

    public NotRemoteException(String message) {
        super(message);
    }

}
