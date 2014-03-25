package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.talend.geat.Configuration;
import org.talend.geat.GitUtils;

import com.google.common.base.Strings;

public class FeaturePush extends AbstractCommand {

    public static final String NAME = "feature-push";

    protected String           featureName;

    public Command parseArgs(String[] args) {
        if (args.length < 2) {
            displayWrongNumberOfParams(args[0]);
        }
        featureName = args[1];

        return this;
    }

    public void run() {
        try {
            // TODO sanity check, repo clean

            Git repo = Git.open(new File(getWorkingDir()));

            // Check if remote
            boolean hasRemote = GitUtils.hasRemote("origin", repo.getRepository());
            if (!hasRemote) {
                System.out.println("No remote defined for this repository. To add one, use:");
                System.out.println("");
                System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates)
                        + "git remote add <name> <url>");
                System.out.println("");
                System.exit(1);
            }

            // Check if local branch exist
            String featureBranchName = Configuration.getInstance().get("featurePrefix") + "/" + featureName;
            Ref ref = repo.getRepository().getRef(featureBranchName);
            if (ref == null) {
                System.out.println("No local branch named '" + featureBranchName + "' exists.");
                System.out.println("");
                System.exit(1);
            }

            // Check if remote branch exist
            if (GitUtils.hasRemoteBranch(repo.getRepository(), featureBranchName)) {
                System.out.println("A remote branch named '" + featureBranchName + "' already exists.");
                System.out.println("If remote branch is related to your local, get newer content with:");
                System.out.println("");
                System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "git fetch");
                System.out.println("");
                System.out.println("And push your latest changes using:");
                System.out.println("");
                System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "git push");
                System.out.println("");
                System.exit(1);
            }

            // set tracking in config:
            Configuration.getInstance().set("branch", featureBranchName, "remote", "origin");
            Configuration.getInstance().set("branch", featureBranchName, "merge", "refs/heads/" + featureBranchName);

            // push
            repo.checkout().setName(featureBranchName).call();
            repo.push().setRemote("origin").add(ref).call();

            // summary
            System.out.println("Summary of actions:");
            System.out.println(" - New remote branch '" + featureBranchName + "' was created");
            System.out.println(" - The local branch '" + featureBranchName
                    + "' was configured to track the remote branch");
            System.out.println(" - You are now on branch '" + featureBranchName + "'");
            System.out.println("");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidRemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransportException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GitAPIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getUsage() {
        return "";
    }

    public String getDescription() {
        return "Push a feature branch to remote to share it";
    }

}
