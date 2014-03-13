package org.talend.geat;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;

import com.google.common.base.Strings;

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
            return exit(exitOnError);
        }

        Git repo = null;
        try {
            repo = Git.open(new File(workingDir));
        } catch (IOException e) {
            if (verbose) {
                System.out.println("'" + workingDir + "' is not a GIT repository.");
            }
            return exit(exitOnError);
        }

        if (checkLevel.ordinal() >= CheckLevel.NO_UNCOMMITTED_CHANGES.ordinal()) {
            try {
                Status status = repo.status().call();
                if (status.hasUncommittedChanges()) {
                    if (verbose) {
                        System.out.println("Your GIT repository has uncommitted changes.");
                        System.out.println("To see these changes, use:\n");
                        System.out
                                .println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + " git status");
                        System.out.println("");
                    }
                    return exit(exitOnError);
                }
            } catch (Exception e) {
                if (verbose) {
                    e.printStackTrace();
                }
                return exit(exitOnError);
            }
        }

        return true;
    }

    public static boolean exit(boolean exitOnError) {
        if (exitOnError) {
            System.out.println("Aborting.");
            System.exit(1);
            return false; // Just to avoid eclipse compilation error
        } else {
            return false;
        }
    }

}
