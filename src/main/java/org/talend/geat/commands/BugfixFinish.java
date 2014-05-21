package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.talend.geat.GitConfiguration;
import org.talend.geat.GitUtils;
import org.talend.geat.exception.IllegalCommandArgumentException;
import org.talend.geat.exception.IncorrectRepositoryStateException;
import org.talend.geat.exception.InterruptedCommandException;

/**
 * Command that finish a bug fix. Means:
 * <ul>
 * <li>fetch start point branch</li>
 * <li>merge bugfix branch on startpoint branch</li>
 * <li>delete bugfix branch</li>
 * </ul>
 * 
 * Branch name must follow the template bugfix/<startpoint>/<bugName>
 */
public class BugfixFinish extends FeatureFinish {

    public static final String NAME = "bugfix-finish";

    // Optionnal param, only required if a bug with the same name exists based on different startpoints
    protected String           target;

    protected BugfixFinish() {
        super();
    }

    public String getDescription() {
        return "Merge a bugfix branch on its startpoint";
    }

    public String getUsage() {
        return "<bugfix-name> [version] [policy]";
    }

    public Command parseArgs(String[] args) throws IllegalCommandArgumentException {
        if (args.length < 2) {
            throw IllegalCommandArgumentException.build(this);
        }
        featureName = args[1];

        if (args.length == 4) {
            target = extractStartpointFromBugName("bugfix/" + args[2] + "/" + featureName);
            mergePolicy = parseMergePolicy(args[3]);
        }

        if (args.length == 3) {
            // Test if first param is a merge policy, if no, consider it as a version:
            try {
                mergePolicy = parseMergePolicy(args[2]);
            } catch (IllegalCommandArgumentException e) {
                target = extractStartpointFromBugName("bugfix/" + args[2] + "/" + featureName);
                mergePolicy = MergePolicy.valueOf(GitConfiguration.getInstance().get("finishmergemode").toUpperCase());
            }
            if (target == null) {
                try {
                    target = guessTarget();
                } catch (IOException | GitAPIException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }

        if (args.length == 2) {
            try {
                target = guessTarget();
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            mergePolicy = MergePolicy.valueOf(GitConfiguration.getInstance().get("finishmergemode").toUpperCase());
        }

        return this;
    }

    /**
     * Regarding existing bugfix branches for this bugname, try to guess the version. Works if only ONE branches like
     * 'bugfix/./bugname' exists.
     */
    private String guessTarget() throws IOException, GitAPIException, IllegalCommandArgumentException {
        Git repo = Git.open(new File(getWorkingDir()));
        List<String> branches = GitUtils.listBranches(repo.getRepository(), "bugfix/.*/" + featureName);
        if (branches.isEmpty()) {
            throw new IllegalCommandArgumentException("No bugfix branch for bug '" + featureName + "'");
        } else if (branches.size() > 1) {
            throw new IllegalCommandArgumentException("More than one branch for bug '" + featureName
                    + "', please specify version");
        } else {
            return extractStartpointFromBugName(branches.get(0));
        }
    }

    public void execute(Writer writer) throws IncorrectRepositoryStateException, IOException, GitAPIException,
            InterruptedCommandException {
        Git repo = Git.open(new File(getWorkingDir()));

        GitUtils.merge(writer, repo, featureName, GitConfiguration.getInstance().get("bugfixPrefix"), target, "BugFix",
                mergePolicy, NAME);
    }

    public String extractStartpointFromBugName(String bugName) throws IllegalCommandArgumentException {
        String[] split = bugName.split("/");
        if (split.length != 3) {
            throw new IllegalCommandArgumentException("");
        }
        String base = split[1];
        if (base.equals("master")) {

        } else if (base.split("\\.").length == 2) {
            base = GitConfiguration.getInstance().get("maintenanceprefix") + "/" + base;
        } else {
            base = GitConfiguration.getInstance().get("releaseprefix") + "/" + base;
        }
        return base;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

}
