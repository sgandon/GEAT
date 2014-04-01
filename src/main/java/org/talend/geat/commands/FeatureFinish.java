package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.talend.geat.Configuration;
import org.talend.geat.GitUtils;
import org.talend.geat.InputsUtils;
import org.talend.geat.SanityCheck;
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.exception.NotRemoteException;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;

public class FeatureFinish extends AbstractCommand {

    public static final String NAME = "feature-finish";

    protected enum MergePolicy {
        REBASE, SQUASH;
    }

    protected String      featureName;

    protected MergePolicy mergePolicy;

    public String getDescription() {
        return "Merge and close a feature branch when work is finished";
    }

    public String getUsage() {
        return "<feature-name> [policy (squash(def)|rebase)]";
    }

    public Command parseArgs(String[] args) {
        if (args.length < 2) {
            displayWrongNumberOfParams(args[0]);
        }
        featureName = args[1];

        try {
            if (args.length >= 3) {
                mergePolicy = MergePolicy.valueOf(args[2].toUpperCase());
            } else {
                mergePolicy = MergePolicy.valueOf(Configuration.getInstance().get("finishmergemode").toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown merge policy '" + args[2] + "'");
            System.out.println("Availables merge policy are:");
            for (MergePolicy current : MergePolicy.values()) {
                System.out.println(" - " + current.name().toLowerCase());
            }
            SanityCheck.exit(true);
        }

        return this;
    }

    public void run() {
        SanityCheck.check(getWorkingDir(), CheckLevel.NO_UNCOMMITTED_CHANGES, true, true);

        try {
            Git repo = Git.open(new File(getWorkingDir()));
            String featureBranchName = Configuration.getInstance().get("featurePrefix") + "/" + featureName;
            boolean hasRemote = GitUtils.hasRemote("origin", repo.getRepository());

            boolean continueAfterConflict = previouslyFinishingThisFeature(featureName);

            // 1. Update sources from remote
            if (hasRemote) {
                try {
                    GitUtils.callFetch(repo.getRepository(), Configuration.getInstance().get("featureStartPoint"));
                    GitUtils.callFetch(repo.getRepository(), featureBranchName);
                } catch (NotRemoteException e) {
                    // We don't care
                }
            }

            // 2. Test if such a branch exists
            if (!GitUtils.hasLocalBranch(repo.getRepository(), featureBranchName)) {
                // TODO if continueAfterConflict==true, then we have an obsolete MERGE file, delete it
                System.out.println("No local branch named '" + featureBranchName + "'");
                System.out.println("To see feature branches that may be finish, use:\n");
                System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates)
                        + "git branch --list feature/*");
                System.out.println("");
                System.exit(1);
            }

            // 3. Try to rebase feature branch
            if (mergePolicy == MergePolicy.REBASE) {
                if (!continueAfterConflict) {
                    // git checkout feature/myfeature
                    repo.checkout().setName(featureBranchName).call();
                    // git rebase master
                    RebaseResult rebaseResult = repo.rebase()
                            .setUpstream(Configuration.getInstance().get("featureStartPoint")).call();

                    if (rebaseResult.getStatus() == RebaseResult.Status.STOPPED) {
                        createMergeAbortedMarker();
                    }
                }

                // git checkout master
                repo.checkout().setName(Configuration.getInstance().get("featureStartPoint")).call();
                // re-init featureBranchName because we just changed it:
                Ref ref = repo.getRepository().getRef(featureBranchName);
                // git merge feature/myfeature
                repo.merge().setFastForward(FastForwardMode.FF_ONLY).include(ref).call();
            } else if (mergePolicy == MergePolicy.SQUASH) {
                if (!continueAfterConflict) {
                    // git checkout master
                    repo.checkout().setName(Configuration.getInstance().get("featureStartPoint")).call();
                    // git merge --squash feature/myfeature
                    Ref ref = repo.getRepository().getRef(featureBranchName);
                    MergeResult mergeResult = repo.merge().setSquash(true).include(ref).call();

                    if (mergeResult.getMergeStatus() == MergeStatus.CONFLICTING) {
                        createMergeAbortedMarker();
                    }

                    String msg = InputsUtils.askUser("Commit message", "Finish feature " + featureName);
                    repo.commit().setMessage(msg).call();
                }
            }

            // 4. Remove feature branch
            repo.branchDelete().setBranchNames(featureBranchName).setForce(mergePolicy == MergePolicy.SQUASH).call();

            System.out.println("Summary of actions:");
            if (hasRemote) {
                System.out.println(" - New commits from 'origin/"
                        + Configuration.getInstance().get("featureStartPoint") + "' has been pulled");
            }
            System.out.println(" - The feature branch '" + featureBranchName + "' was rebased into '"
                    + Configuration.getInstance().get("featureStartPoint") + "'");
            System.out.println(" - Feature branch '" + featureBranchName + "' has been removed");
            System.out.println(" - You are now on branch '" + Configuration.getInstance().get("featureStartPoint")
                    + "'");
            System.out.println("");
            if (hasRemote) {
                System.out.println("Now, your new feature is ready to be pushed. To do this, use:");
                System.out.println("");
                System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "git push origin "
                        + Configuration.getInstance().get("featureStartPoint"));
                System.out.println("");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RefAlreadyExistsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RefNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidRefNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CheckoutConflictException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GitAPIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean previouslyFinishingThisFeature(String featureName) throws IOException {
        File mergeMarker = new File(getGeatConfigFolfer(), "MERGE");
        if (mergeMarker.exists()) {
            String readFirstLine = Files.readFirstLine(mergeMarker, Charsets.UTF_8);
            String[] split = readFirstLine.split(" ");
            if (split.length == 4 && split[0].equals("MERGE") && split[2].equals("IN")) {
                if (split[1].equals(featureName)) {
                    mergeMarker.delete();
                    return true;
                } else {
                    System.out.println("Previous finish of " + split[1] + " was aborted because of conflicts.");
                    System.out.println("Please finish this feature first.");
                    System.out.println("You can then complete the finish by running it again.\n");
                    System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "geat "
                            + FeatureFinish.NAME + " " + split[1] + " <policy>");
                    System.out.println("");
                    System.exit(1);
                    return false;
                }
            } else {
                // Malformed MERGE file:
                System.out.println("WARN: previous file " + mergeMarker.getAbsolutePath() + " is malformed.");
                System.out.println(readFirstLine);
                System.out.println("Deleting the file");
                mergeMarker.delete();
                return false;
            }
        } else {
            return false;
        }
    }

    private void createMergeAbortedMarker() throws IOException {
        File mergeMarker = new File(getGeatConfigFolfer(), "MERGE");
        Files.createParentDirs(mergeMarker);
        Files.touch(mergeMarker);
        Files.write(
                ("MERGE " + featureName + " IN " + Configuration.getInstance().get("featureStartPoint")).getBytes(),
                mergeMarker);

        System.out.println("There were merge conflicts. To see more details, use:\n");
        System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "git status");
        System.out.println("");
        System.out.println("When resolved, you can then complete the finish by running it again.\n");
        System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "geat " + FeatureFinish.NAME
                + " " + featureName + " " + mergePolicy.toString().toLowerCase());
        System.out.println("");
        System.exit(1);
    }

    private File getGeatConfigFolfer() {
        File folder = new File(new File(getWorkingDir(), ".git"), ".geat");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }

}
