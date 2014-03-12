package org.talend.geat.commands;

public abstract class AbstractCommand implements Command {

    private String workingDir;

    public Command setWorkingDir(String path) {
        this.workingDir = path;
        return this;
    }

    protected String getWorkingDir() {
        return workingDir;
    }

}
