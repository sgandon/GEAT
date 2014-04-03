package org.talend.geat.exception;


/**
 * When a command has to stop, and, in general, can be re-launch later.
 * 
 * When launched, the repository has been changed.
 * 
 * Examples: conflicts appears in the middle of some operations
 */
public class InterruptedCommandException extends Exception {

    private StringBuffer details = new StringBuffer();

    public InterruptedCommandException(Throwable cause) {
        super(cause);
    }

    public InterruptedCommandException(String message) {
        super(message);
        addLine(message);
    }

    public StringBuffer addLine(String str) {
        if (details.length() > 0) {
            details.append("\n");
        }
        return details.append(str);
    }

    public String getDetails() {
        return details.toString();
    }
}
