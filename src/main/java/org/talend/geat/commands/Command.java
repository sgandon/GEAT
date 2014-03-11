package org.talend.geat.commands;

public interface Command {

    public void run(String[] args);

    public String getDescription();

}