package org.talend.geat.commands;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.exception.IllegalCommandArgumentException;
import org.talend.geat.exception.IncorrectRepositoryStateException;

/**
 * Command that finish a bug fix. Means:
 * <ul>
 * <li>fetch start point branch</li>
 * <li>merge bugfix branch on startpoint branch</li>
 * <li>delete bugfix branch</li>
 * </ul>
 */
public class BugfixFinish extends Command {

    public static final String NAME = "bugfix-finish";

    // the name of the bug (bugtracker id for example), will be used for the branch name:
    protected String           bugName;

    // the start point of the new branch:
    protected String           startPoint;

    protected BugfixFinish() {
        super();
    }

    public String getCommandName() {
        return NAME;
    }

    public Command parseArgs(String[] args) throws IllegalCommandArgumentException {
        if (args.length != 2 && args.length != 3) {
            throw IllegalCommandArgumentException.build(this);
        }
        bugName = args[1];

        if (args.length == 3) {
            startPoint = args[2];
        }

        return this;
    }

    public String getDescription() {
        return "Merge a bugfix branch on its startpoint";
    }

    public String getUsage() {
        return "<bugfix-name> [merge-target] policy";
    }

    @Override
    public CheckLevel getCheckLevel() {
        return CheckLevel.NO_UNCOMMITTED_CHANGES;
    }

    public void execute(Writer writer) throws IncorrectRepositoryStateException, IOException, GitAPIException {

    }

}
