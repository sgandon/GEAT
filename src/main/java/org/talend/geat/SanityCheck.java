package org.talend.geat;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.talend.geat.exception.IncorrectRepositoryStateException;

import com.google.common.base.Strings;

/**
 * Used to check if the working dir is ready for what we need.
 * 
 * Different check levels are availables, regarding the command to process.
 */
public class SanityCheck {

    public enum CheckLevel {
        NONE, // No check
        GIT_REPO_ONLY, // only checks that the folder is a initialized git repository
        NO_UNCOMMITTED_CHANGES; // in addition to GIT_REPO_ONLY, also checks that there are now uncommitted changes
    }

    public static void check(CheckLevel checkLevel) throws IncorrectRepositoryStateException {
        String workingDir = System.getProperty("user.dir");
        if (checkLevel == CheckLevel.NONE) {
            return;
        }

        File repoPath = new File(workingDir);
        if (!repoPath.exists() || !repoPath.isDirectory()) {
            throw new IncorrectRepositoryStateException("'" + workingDir + "' is not a folder.");
        }

        Git repo = null;
        try {
            repo = Git.open(new File(workingDir));
        } catch (IOException e) {
            throw new IncorrectRepositoryStateException("'" + workingDir + "' is not a GIT repository.");
        }

        if (checkLevel.ordinal() >= CheckLevel.NO_UNCOMMITTED_CHANGES.ordinal()) {
            try {
                Status status = repo.status().call();
                if (status.hasUncommittedChanges()) {
                    IncorrectRepositoryStateException iwse = new IncorrectRepositoryStateException(
                            "Your GIT repository has uncommitted changes.");
                    iwse.addLine("To see these changes, use:\n");
                    iwse.addLine(Strings.repeat(" ", Configuration.INSTANCE.getAsInt("geat.indentForCommandTemplates"))
                            + " git status");
                    throw iwse;
                }
            } catch (NoWorkTreeException e) {
                throw new IncorrectRepositoryStateException(e);
            } catch (GitAPIException e) {
                throw new IncorrectRepositoryStateException(e);
            }
        }
    }

}
