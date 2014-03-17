package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.talend.geat.Configuration;
import org.talend.geat.GitUtils;
import org.talend.geat.InputsUtils;
import org.talend.geat.SanityCheck;
import org.talend.geat.SanityCheck.CheckLevel;

import com.google.common.base.Strings;

public class FeatureStart extends AbstractCommand {

    public String getDescription() {
        return "Create a branch to work on a new feature";
    }

    public String getUsage() {
        return "<feature-name>";
    }

    public int getArgsNumber() {
        return 1;
    }

    public void run(String[] args) {
        if (!SanityCheck.check(getWorkingDir(), CheckLevel.NO_UNCOMMITTED_CHANGES, true, false)
                && !InputsUtils.askUserAsBoolean("Proceed anyway")) {
            SanityCheck.exit(true);
        }
        try {
            Git repo = Git.open(new File(getWorkingDir()));
            String featureBranchName = Configuration.featurePrefix + "/" + args[1];
            boolean hasRemote = GitUtils.hasRemote("origin", repo.getRepository());

            // Test if such a branch exists locally:
            Ref ref = repo.getRepository().getRef(featureBranchName);
            if (ref != null) {
                System.out.println("A local branch named '" + featureBranchName + "' already exist.");
                System.out.println("");
                System.exit(1);
            }

            if (hasRemote) {
                // Test if branch exist remotely:
                Collection<Ref> lsRemoteBranches = repo.lsRemote().setTags(false).setRemote("origin").call();
                for (Ref current : lsRemoteBranches) {
                    if (current.getName().equals("refs/heads/" + featureBranchName)) {
                        System.out.println("A remote branch named '" + featureBranchName + "' already exist.");
                        System.out.println("To checkout this branch locally, use:");
                        System.out.println("");
                        System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates)
                                + "git fetch && git checkout " + featureBranchName);
                        System.out.println("");
                        System.exit(1);
                    }
                }

                // git checkout master
                repo.checkout().setName(Configuration.featureStartPoint).call();

                // git pull --rebase origin
                // 1. git fetch
                repo.fetch()
                        .setRefSpecs(
                                new RefSpec("refs/heads/" + Configuration.featureStartPoint + ":refs/remotes/origin/"
                                        + Configuration.featureStartPoint)).setRemote("origin").call();
                // 2. git merge ff
                Ref refOriginMaster = repo.getRepository().getRef("origin/master");
                repo.merge().setFastForward(FastForwardMode.FF_ONLY).include(refOriginMaster).call();
            }

            repo.checkout().setCreateBranch(true).setStartPoint(Configuration.featureStartPoint)
                    .setName(featureBranchName).call();

            System.out.println("Summary of actions:");
            System.out.println(" - A new branch '" + featureBranchName + "' was created, based on '"
                    + Configuration.featureStartPoint + "'");
            System.out.println(" - You are now on branch '" + featureBranchName + "'");
            System.out.println("");
            System.out.println("Now, start committing on your feature. When done, use:");
            System.out.println("");
            System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "geat "
                    + FeatureFinish.NAME + " " + args[1] + " <policy>");
            System.out.println("");
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
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
