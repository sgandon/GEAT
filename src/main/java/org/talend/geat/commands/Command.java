package org.talend.geat.commands;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.talend.geat.Configuration;
import org.talend.geat.SanityCheck;
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.exception.IllegalCommandArgumentException;
import org.talend.geat.exception.IncorrectRepositoryStateException;
import org.talend.geat.exception.InterruptedCommandException;
import org.talend.geat.io.AutoFlushLineWriter;

public abstract class Command {

    private boolean callable = true;

    private String workingDir;

    private Writer  writer   = new AutoFlushLineWriter(new OutputStreamWriter(System.out));

    public Command() {
        super();

        setWorkingDir(System.getProperty("user.dir"));
    }

    public Command setWorkingDir(String path) {
        this.workingDir = path;
        return this;
    }

    protected String getWorkingDir() {
        return workingDir;
    }

    public abstract String getUsage();

    public abstract String getDescription();

    protected abstract void execute(Writer writer) throws IncorrectRepositoryStateException, IOException,
            GitAPIException, InterruptedCommandException;

    public void run() throws IncorrectRepositoryStateException, IOException, GitAPIException,
            InterruptedCommandException {
        checkCallable();

        SanityCheck.check(workingDir, CheckLevel.GIT_REPO_ONLY);

        callable = false;

        execute(this.writer);
    }

    protected void checkCallable() {
        if (!callable) {
            throw new IllegalStateException("Command was called in the wrong state");
        }
    }

    public Command parseArgs(String[] args) throws IllegalCommandArgumentException {
        return this;
    }

    public abstract String getCommandName();

    public Writer getWriter() {
        return writer;
    }

    public Command setWriter(Writer writer) {
        this.writer = writer;
        return this;
    }

}
