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

public class FeatureFinish extends AbstractCommand {

    public String getDescription() {
        return "Merge and close a feature branch when work is finished";
    }

    public String getUsage() {
        return "<feature-name>";
    }

    public int getArgsNumber() {
        return 1;
    }

    public void run(String[] args) {
        try {
            Git repo = Git.open(new File(getWorkingDir()));
            String featureBranchName = Configuration.featurePrefix + "/" + args[1];

            // 1. Test if such a branch exists
            Ref ref = repo.getRepository().getRef(featureBranchName);
            if (ref == null) {
                System.out.println("No local branch named '" + featureBranchName + "'");
                System.exit(1);
            }

            // 2. Try to rebase the branch
            repo.checkout().setName(featureBranchName).call();
            repo.rebase().setUpstream(Configuration.featureStartPoint).call();
            repo.checkout().setName(Configuration.featureStartPoint).call();
            repo.merge().setFastForward(FastForwardMode.FF_ONLY).include(ref).call();

            // 3. Remove feature branch
            repo.branchDelete().setBranchNames(featureBranchName).call();
            
            System.out.println("Summary of actions:");
            System.out.println(" - The feature branch '" + featureBranchName + "' was merged into '"
                    + Configuration.featureStartPoint + "'");
            System.out.println(" - Feature branch '" + featureBranchName + "' has been removed");
            System.out.println(" - You are now on branch '" + Configuration.featureStartPoint + "'");
            System.out.println("");
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
