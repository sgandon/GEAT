package org.talend.geat;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;

public class SanityCheck {

    public enum CheckLevel {
        GIT_REPO_ONLY, NO_UNCOMMITTED_CHANGES, CLEAN;
    }

    public static boolean check(String workingDir, CheckLevel checkLevel, boolean verbose, boolean exitOnError) {
        File repoPath = new File(workingDir);
        if (!repoPath.exists() || !repoPath.isDirectory()) {
            if (verbose) {
                System.out.println("'" + workingDir + "' is not a folder.");
            }
            if (exitOnError) {
                System.exit(1);
            } else {
                return false;
            }
        }

        Git repo = null;
        try {
            repo = Git.open(new File(workingDir));
        } catch (IOException e) {
            if (verbose) {
                System.out.println("'" + workingDir + "' is not a GIT repository.");
            }
            if (exitOnError) {
                System.exit(1);
            } else {
                return false;
            }
        }

        if (checkLevel.ordinal() >= CheckLevel.NO_UNCOMMITTED_CHANGES.ordinal()) {
            try {
                Status status = repo.status().call();
                if (status.hasUncommittedChanges()) {
                    if (verbose) {
                        System.out.println("Your GIT repository has uncommitted changes.");
                    }
                    if (exitOnError) {
                        System.exit(1);
                    } else {
                        return false;
                    }
                }
            } catch (Exception e) {
                if (verbose) {
                    e.printStackTrace();
                }
                if (exitOnError) {
                    System.exit(1);
                } else {
                    return false;
                }
            }
        }

        return true;
    }

}
