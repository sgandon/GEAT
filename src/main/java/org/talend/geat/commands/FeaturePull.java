package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.talend.geat.Configuration;
import org.talend.geat.GitUtils;
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.exception.IllegalCommandArgumentException;
import org.talend.geat.exception.IncorrectRepositoryStateException;

import com.google.common.base.Strings;

/**
 * Use this command to fetch locally a remote feature branch.
 */
public class FeaturePull extends Command {

    public static final String NAME = "feature-pull";

    protected String           featureName;

    protected FeaturePull() {
        super();
    }

    public String getCommandName() {
        return NAME;
    }

    public Command parseArgs(String[] args) throws IllegalCommandArgumentException {
        if (args.length < 2) {
            throw IllegalCommandArgumentException.build(this);
        }
        featureName = args[1];

        return this;
    }

    @Override
    public CheckLevel getCheckLevel() {
        return CheckLevel.NO_UNCOMMITTED_CHANGES;
    }

    public void execute(Writer writer) throws IncorrectRepositoryStateException, IOException, GitAPIException {
        Git repo = Git.open(new File(getWorkingDir()));

        // Check if remote
        boolean hasRemote = GitUtils.hasRemote("origin", repo.getRepository());
        if (!hasRemote) {
            IncorrectRepositoryStateException irse = new IncorrectRepositoryStateException(
                    "No remote defined for this repository. To add one, use:");
            irse.addLine("");
            irse.addLine(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "git remote add <name> <url>");
            throw irse;
        }

        String featureBranchName = Configuration.getInstance().get("featurePrefix") + "/" + featureName;

        // Update branch:
        boolean created = GitUtils.callFetch(repo.getRepository(), featureBranchName);

        writer.write("Summary of actions:");
        if (created) {
            writer.write(" - New local branch '" + featureBranchName + "' based on '" + "origin" + "''s "
                    + featureBranchName + " was created.");
        } else {
            writer.write(" - Local branch '" + featureBranchName + "' based on '" + "origin" + "''s "
                    + featureBranchName + " was updated.");
        }
    }

    public String getUsage() {
        return "<feature-name>";
    }

    public String getDescription() {
        return "Pull a feature branch from remote";
    }

}
