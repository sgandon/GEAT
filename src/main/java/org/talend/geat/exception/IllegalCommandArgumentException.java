package org.talend.geat.exception;

import org.talend.geat.GitConfiguration;
import org.talend.geat.commands.Command;

import com.google.common.base.Strings;

/**
 * When arguments don't match what the command expects.
 * 
 * When launched, the repository has NOT been changed.
 */
public class IllegalCommandArgumentException extends Exception {

    public IllegalCommandArgumentException(String message) {
        super(message);
    }

    public static IllegalCommandArgumentException build(Command command) {
        IllegalCommandArgumentException toReturn = new IllegalCommandArgumentException(
                "Wrong number of parameters for this command!\nUsage is:\n"
                        + Strings.repeat(" ", GitConfiguration.indentForCommandTemplates) + command.getCommandName() + " "
                        + command.getUsage());
        return toReturn;
    }
}
