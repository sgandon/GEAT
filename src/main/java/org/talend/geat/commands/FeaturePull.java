package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.talend.geat.Configuration;
import org.talend.geat.GitUtils;
import org.talend.geat.SanityCheck;
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.exception.NotRemoteException;

import com.google.common.base.Strings;

public class FeaturePull extends AbstractCommand {

    public static final String NAME = "feature-pull";

    protected String           featureName;

    public Command parseArgs(String[] args) {
        if (args.length < 2) {
            displayWrongNumberOfParams(args[0]);
        }
        featureName = args[1];

        return this;
    }

    public void run() {
        SanityCheck.check(getWorkingDir(), CheckLevel.NO_UNCOMMITTED_CHANGES, true, true);

        try {
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

            String featureBranchName = Configuration.getInstance().get("featurePrefix") + "/" + featureName;

            // Update branch:
            boolean created = GitUtils.callFetch(repo.getRepository(), featureBranchName);

            System.out.println("Summary of actions:");
            if (created) {
                System.out.println(" - New local branch '" + featureBranchName + "' based on '" + "origin" + "''s "
                        + featureBranchName + " was created.");
            } else {
                System.out.println(" - Local branch '" + featureBranchName + "' based on '" + "origin" + "''s "
                        + featureBranchName + " was updated.");
            }
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
        } catch (NotRemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getUsage() {
        return "<feature-name>";
    }

    public String getDescription() {
        return "Pull a feature branch from remote";
    }

}
