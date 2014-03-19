package org.talend.geat.commands;


public interface Command {

    public Command setWorkingDir(String path);

    public void run();

    public String getDescription();

    public String getUsage();

    public Command parseArgs(String[] args);

}
