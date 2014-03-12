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
            System.out.println("Aborting.");
            System.exit(1);
        }
        try {
            Git repo = Git.open(new File(getWorkingDir()));
            String featureBranchName = Configuration.featurePrefix + "/" + args[1];

            repo.checkout().setCreateBranch(true).setStartPoint(Configuration.featureStartPoint)
                    .setName(featureBranchName).call();

            System.out.println("Summary of actions:");
            System.out.println(" - A new branch '" + featureBranchName + "' was created, based on '"
                    + Configuration.featureStartPoint + "'");
            System.out.println(" - You are now on branch '" + featureBranchName + "'");
            System.out.println("");
            System.out.println("Now, start committing on your feature. When done, use:");
            System.out.println("");
            System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + "geat feature-finish "
                    + args[1]);
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
