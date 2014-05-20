package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.talend.geat.GitConfiguration;
import org.talend.geat.GitUtils;
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.exception.IllegalCommandArgumentException;
import org.talend.geat.exception.IncorrectRepositoryStateException;
import org.talend.geat.exception.InterruptedCommandException;

/**
 * Command that finish a feature. Means:
 * <ul>
 * <li>fetch feature branch</li>
 * <li>fetch develop branch</li>
 * <li>merge feature branch into develop branch (differents policy can be applied here)</li>
 * <li>delete feature branch</li>
 * </ul>
 */
public class FeatureFinish extends Command {

    public static final String NAME = "feature-finish";

    protected String           featureName;

    protected MergePolicy      mergePolicy;

    protected FeatureFinish() {
        super();
    }

    public String getCommandName() {
        return NAME;
    }

    public String getDescription() {
        return "Merge and close a feature branch when work is finished";
    }

    public String getUsage() {
        return "<feature-name> [policy (squash|rebase), default="
                + GitConfiguration.getInstance().get("finishmergemode") + "]";
    }

    public Command parseArgs(String[] args) throws IllegalCommandArgumentException {
        if (args.length < 2) {
            throw IllegalCommandArgumentException.build(this);
        }
        featureName = args[1];

        try {
            if (args.length >= 3) {
                mergePolicy = MergePolicy.valueOf(args[2].toUpperCase());
            } else {
                mergePolicy = MergePolicy.valueOf(GitConfiguration.getInstance().get("finishmergemode").toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            StringBuilder sb = new StringBuilder();

            sb.append("Unknown merge policy '" + args[2] + "'");
            sb.append("Availables merge policy are:");
            for (MergePolicy current : MergePolicy.values()) {
                sb.append(" - " + current.name().toLowerCase());
            }
            throw new IllegalCommandArgumentException(sb.toString());
        }

        return this;
    }

    @Override
    public CheckLevel getCheckLevel() {
        return CheckLevel.NO_UNCOMMITTED_CHANGES;
    }

    public void execute(Writer writer) throws IncorrectRepositoryStateException, IOException, GitAPIException,
            InterruptedCommandException {
        Git repo = Git.open(new File(getWorkingDir()));

        GitUtils.merge(writer, repo, featureName, GitConfiguration.getInstance().get("featurePrefix"), GitConfiguration
                .getInstance().get("featureStartPoint"), "feature", mergePolicy, NAME);
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public Enum<MergePolicy> getMergePolicy() {
        return mergePolicy;
    }

    public void setMergePolicy(MergePolicy mergePolicy) {
        this.mergePolicy = mergePolicy;
    }

}
