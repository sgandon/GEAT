package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.talend.geat.Configuration;
import org.talend.geat.GitUtils;
import org.talend.geat.InputsUtils;
import org.talend.geat.SanityCheck;
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.exception.IllegalCommandArgumentException;
import org.talend.geat.exception.IncorrectRepositoryStateException;

import com.google.common.base.Strings;

/**
 * Command that starts a feature. Means:
 * <ul>
 * <li>fetch develop branch</li>
 * <li>create a local branch based on develop branch</li>
 * <li>checkout new feature branch</li>
 * </ul>
 */
public class FeatureStart extends Command {

    public static final String NAME = "feature-start";

    protected String           featureName;

    protected FeatureStart() {
        super();
    }

    public String getCommandName() {
        return NAME;
    }

    public Command parseArgs(String[] args) throws IllegalCommandArgumentException {
        if (args.length != 2) {
            throw IllegalCommandArgumentException.build(this);
        }
        featureName = args[1];

        return this;
    }

    public String getDescription() {
        return "Create a branch to work on a new feature";
    }

    public String getUsage() {
        return "<feature-name>";
    }

    @Override
    public CheckLevel getCheckLevel() {
        return CheckLevel.GIT_REPO_ONLY;
    }

    public void execute(Writer writer) throws IncorrectRepositoryStateException, IOException, GitAPIException {
        try {
            SanityCheck.check(getWorkingDir(), CheckLevel.NO_UNCOMMITTED_CHANGES);
        } catch (IncorrectRepositoryStateException e) {
            if (!InputsUtils.askUserAsBoolean(e.getDetails() + "\n\nProceed anyway")) {
                return;
            }
        }

        Git repo = Git.open(new File(getWorkingDir()));
        String featureBranchName = Configuration.getInstance().get("featurePrefix") + "/" + featureName;
        boolean hasRemote = GitUtils.hasRemote("origin", repo.getRepository());

        // Test if such a branch exists locally:
        if (GitUtils.hasLocalBranch(repo.getRepository(), featureBranchName)) {
            throw new IncorrectRepositoryStateException("A local branch named '" + featureBranchName
                    + "' already exist.");
        }

        if (hasRemote) {
            // Test if branch exist remotely:
            if (GitUtils.hasRemoteBranch(repo.getRepository(), featureBranchName)) {
                IncorrectRepositoryStateException irse = new IncorrectRepositoryStateException(
                        "A remote branch named '" + featureBranchName + "' already exist.");
                irse.addLine("To checkout this branch locally, use:");
                irse.addLine("");
                irse.addLine(Strings.repeat(" ", Configuration.indentForCommandTemplates)
                        + "git fetch && git checkout " + featureBranchName);
                throw irse;
            }

            // git checkout master
            repo.checkout().setName(Configuration.getInstance().get("featureStartPoint")).call();

            // git pull --rebase origin
            // 1. git fetch
            repo.fetch()
                    .setRefSpecs(
                            new RefSpec("refs/heads/" + Configuration.getInstance().get("featureStartPoint")
                                    + ":refs/remotes/origin/" + Configuration.getInstance().get("featureStartPoint")))
                    .setRemote("origin").call();
            // 2. git merge ff
            Ref refOriginMaster = repo.getRepository().getRef("origin/master");
            repo.merge().setFastForward(FastForwardMode.FF_ONLY).include(refOriginMaster).call();
        }

        repo.checkout().setCreateBranch(true).setStartPoint(Configuration.getInstance().get("featureStartPoint"))
                .setName(featureBranchName).call();

        writer.write("Summary of actions:");
        writer.write(" - A new branch '" + featureBranchName + "' was created, based on '"
                + Configuration.getInstance().get("featureStartPoint") + "'");
        writer.write(" - You are now on branch '" + featureBranchName + "'");
        writer.write("");
        writer.write("Now, start committing on your feature. When done, use:");
        writer.write("");
        writer.write(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "geat " + FeatureFinish.NAME + " "
                + featureName + " <policy>");
        writer.write("");
        writer.write("To share this branch, use:");
        writer.write("");
        writer.write(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "geat " + FeaturePush.NAME + " "
                + featureName);
    }

}
