package org.talend.geat.commands;

public interface Command {

    public Command setWorkingDir(String path);

    public void run(String[] args);

    public String getDescription();

    public int getArgsNumber();

    public String getUsage();

}
