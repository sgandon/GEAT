package org.talend.geat.exception;


/**
 * Working dir is not ready to use.
 * 
 * Examples: not clean, no remote defined, etc...
 */
public class IncorrectRepositoryStateException extends Exception {

    private StringBuffer details = new StringBuffer();

    public IncorrectRepositoryStateException(Throwable cause) {
        super(cause);
    }

    public IncorrectRepositoryStateException(String message) {
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
