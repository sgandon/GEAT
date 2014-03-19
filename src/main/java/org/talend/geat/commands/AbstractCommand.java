package org.talend.geat.commands;

import org.talend.geat.Configuration;
import org.talend.geat.SanityCheck;

import com.google.common.base.Strings;

public abstract class AbstractCommand implements Command {

    private String workingDir;

    public Command setWorkingDir(String path) {
        this.workingDir = path;
        return this;
    }

    protected String getWorkingDir() {
        return workingDir;
    }

    protected void displayWrongNumberOfParams(String name) {
        System.out.println("Wrong number of parameters for this command!\nUsage is:\n");
        System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + name + " " + getUsage()
                + "\n");
        SanityCheck.exit(true);
    }

    public Command parseArgs(String[] args) {
        return this;
    }
}
