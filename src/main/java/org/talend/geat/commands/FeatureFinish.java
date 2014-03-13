package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
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

import com.google.common.base.Strings;

public class FeatureFinish extends AbstractCommand {

    public static final String NAME = "feature-finish";

    private enum MergePolicy {
        REBASE, SQUASH;
    }

    public String getDescription() {
        return "Merge and close a feature branch when work is finished";
    }

    public String getUsage() {
        return "<feature-name> <policy (squash(def), rebase)>";
    }

    public int getArgsNumber() {
        return 2;
    }

    public void run(String[] args) {
        SanityCheck.check(getWorkingDir(), CheckLevel.NO_UNCOMMITTED_CHANGES, true, true);

        // ---------------------------------------------------------------------------------
        // Checkin merge policy:
        // ---------------------------------------------------------------------------------
        MergePolicy mergePolicy = null;
        try {
            mergePolicy = MergePolicy.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown merge policy '" + args[2] + "'");
            System.out.println("Availables merge policy are:");
            for (MergePolicy current : MergePolicy.values()) {
                System.out.println(" - " + current.name().toLowerCase());
            }
            SanityCheck.exit(true);
        }
        // ---------------------------------------------------------------------------------

        try {
            Git repo = Git.open(new File(getWorkingDir()));
            String featureBranchName = Configuration.featurePrefix + "/" + args[1];
            boolean hasRemote = GitUtils.hasRemote("origin", repo.getRepository());

            // 1. Test if such a branch exists
            Ref ref = repo.getRepository().getRef(featureBranchName);
            if (ref == null) {
                System.out.println("No local branch named '" + featureBranchName + "'");
                System.out.println("To see feature branches that may be finish, use:\n");
                System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates)
                        + "git branch --list feature/*");
                System.out.println("");
                System.exit(1);
            }

            // 2. Update sources from remote
            repo.checkout().setName(Configuration.featureStartPoint).call(); // git checkout master
            if (hasRemote) {
                repo.pull().setRebase(true).setRemote("origin").call(); // git pull --rebase origin
            }

            // 3. Try to rebase feature branch
            if (mergePolicy == MergePolicy.REBASE) {
                repo.checkout().setName(featureBranchName).call(); // git checkout feature/myfeature
                repo.rebase().setUpstream(Configuration.featureStartPoint).call(); // git rebase master
                repo.checkout().setName(Configuration.featureStartPoint).call(); // git checkout master
                // re-init featureBranchName because we just changed it:
                ref = repo.getRepository().getRef(featureBranchName);
                repo.merge().setFastForward(FastForwardMode.FF_ONLY).include(ref).call(); // git merge feature/myfeature
            } else if (mergePolicy == MergePolicy.SQUASH) {
                repo.checkout().setName(Configuration.featureStartPoint).call(); // git checkout master
                repo.merge().setSquash(true).include(ref).call(); // git merge --squash feature/myfeature
                String msg = InputsUtils.askUser("Commit message", "Finish feature " + args[1]);
                repo.commit().setMessage(msg).call();
            }

            // 4. Remove feature branch
            repo.branchDelete().setBranchNames(featureBranchName).setForce(mergePolicy == MergePolicy.SQUASH).call();

            System.out.println("Summary of actions:");
            if (hasRemote) {
                System.out.println(" - New commits from 'origin/" + Configuration.featureStartPoint
                        + "' has been pulled");
            }
            System.out.println(" - The feature branch '" + featureBranchName + "' was rebased into '"
                    + Configuration.featureStartPoint + "'");
            System.out.println(" - Feature branch '" + featureBranchName + "' has been removed");
            System.out.println(" - You are now on branch '" + Configuration.featureStartPoint + "'");
            System.out.println("");
            if (hasRemote) {
                System.out.println("Now, your new feature is ready to be pushed. To do this, use:");
                System.out.println("");
                System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "git push origin "
                        + Configuration.featureStartPoint);
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

}
