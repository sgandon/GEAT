package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.talend.geat.GitConfiguration;
import org.talend.geat.GitUtils;
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.exception.IllegalCommandArgumentException;
import org.talend.geat.exception.IncorrectRepositoryStateException;

import com.google.common.base.Strings;

/**
 * Use this command to push to remote a feature branch (in order to share with co-workers).
 */
public class FeaturePush extends Command {

    public static final String NAME = "feature-push";

    protected String           featureName;

    protected FeaturePush() {
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
            irse.addLine(Strings.repeat(" ", GitConfiguration.indentForCommandTemplates) + "git remote add <name> <url>");
            throw irse;
        }

        // Check if local branch exist
        String featureBranchName = GitConfiguration.getInstance().get("featurePrefix") + "/" + featureName;
        Ref ref = repo.getRepository().getRef(featureBranchName);
        if (ref == null) {
            IncorrectRepositoryStateException irse = new IncorrectRepositoryStateException("No local branch named '"
                    + featureBranchName + "' exists.");
            throw irse;
        }

        // Check if remote branch exist
        if (GitUtils.hasRemoteBranch(repo.getRepository(), featureBranchName)) {
            IncorrectRepositoryStateException irse = new IncorrectRepositoryStateException("A remote branch named '"
                    + featureBranchName + "' already exists.");
            irse.addLine("If remote branch is related to your local, get newer content with:");
            irse.addLine("");
            irse.addLine(Strings.repeat(" ", GitConfiguration.indentForCommandTemplates) + "git fetch");
            irse.addLine("");
            irse.addLine("And push your latest changes using:");
            irse.addLine("");
            irse.addLine(Strings.repeat(" ", GitConfiguration.indentForCommandTemplates) + "git push");
            throw irse;
        }

        // set tracking in config:
        GitConfiguration.getInstance().set("branch", featureBranchName, "remote", "origin");
        GitConfiguration.getInstance().set("branch", featureBranchName, "merge", "refs/heads/" + featureBranchName);

        // push
        repo.checkout().setName(featureBranchName).call();
        repo.push().setRemote("origin").add(ref).call();

        // summary
        writer.write("Summary of actions:");
        writer.write(" - New remote branch '" + featureBranchName + "' was created");
        writer.write(" - The local branch '" + featureBranchName + "' was configured to track the remote branch");
        writer.write(" - You are now on branch '" + featureBranchName + "'");
    }

    public String getUsage() {
        return "<feature-name>";
    }

    public String getDescription() {
        return "Push a feature branch to remote to share it";
    }

}
