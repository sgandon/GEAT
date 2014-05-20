package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.talend.geat.GitConfiguration;
import org.talend.geat.GitUtils;
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

    protected BugfixFinish() {
        super();
    }

    public String getDescription() {
        return "Merge a bugfix branch on its startpoint";
    }

    public String getUsage() {
        return "<bugfix-name> [merge-target] policy";
    }

    public void execute(Writer writer) throws IncorrectRepositoryStateException, IOException, GitAPIException,
            InterruptedCommandException {
        Git repo = Git.open(new File(getWorkingDir()));

        String target = extractStartpointFromBugName(featureName);

        GitUtils.merge(writer, repo, featureName, GitConfiguration.getInstance().get("bugfixPrefix"), target, "BugFix",
                mergePolicy, NAME);
    }

    public String extractStartpointFromBugName(String bugName) {
        String[] split = bugName.split("/");
        String base = split[1];
        if (base.equals("master")) {

        } else if (base.split("\\.").length == 2) {
            base = GitConfiguration.getInstance().get("maintenanceprefix") + "/" + base;
        } else {
            base = GitConfiguration.getInstance().get("releaseprefix") + "/" + base;
        }
        return base;
    }

}
