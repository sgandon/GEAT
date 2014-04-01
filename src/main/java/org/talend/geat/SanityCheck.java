package org.talend.geat;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.talend.geat.exception.IncorrectRepositoryStateException;

import com.google.common.base.Strings;

public class SanityCheck {

    public enum CheckLevel {
        GIT_REPO_ONLY, NO_UNCOMMITTED_CHANGES;
    }

    public static void check(String workingDir, CheckLevel checkLevel) throws IncorrectRepositoryStateException {
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
                    iwse.addLine(Strings.repeat(" ", Configuration.indentForCommandTemplates) + " git status");
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
