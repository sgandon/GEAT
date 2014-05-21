package org.talend.geat;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.talend.geat.commands.MergePolicy;
import org.talend.geat.exception.IncorrectRepositoryStateException;
import org.talend.geat.exception.InterruptedCommandException;
import org.talend.geat.exception.NotRemoteException;
import org.talend.geat.jgit.ListBranchCommand;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;

/**
 * Utility class to mutualize some current GIT operation.
 */
public class GitUtils {

    public static boolean hasRemote(String remoteName, Repository repository) {
        try {
            List<RemoteConfig> remoteConfigs = RemoteConfig.getAllRemoteConfigs(repository.getConfig());
            for (RemoteConfig current : remoteConfigs) {
                if (current.getName().equals(remoteName)) {
                    return true;
                }
            }
        } catch (URISyntaxException e) {
            System.out.println("WARN: " + e.getMessage());
        }
        return false;
    }

    public static boolean hasLocalBranch(Repository repository, String branch) throws IOException {
        return repository.getRef(branch) != null;
    }

    public static boolean hasRemoteBranch(Repository repository, String branch) throws GitAPIException {
        boolean hasRemote = GitUtils.hasRemote("origin", repository);
        if (!hasRemote) {
            return false;
        }

        Git git = new Git(repository);

        Collection<Ref> lsRemoteBranches = git.lsRemote().setTags(false).setRemote("origin").call();
        for (Ref current : lsRemoteBranches) {
            if (current.getName().equals("refs/heads/" + branch)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Choosen policy is git pull --rebase
     * 
     * @return true if local branch has been created, false if branch previously exists and was only updated
     * @throws NotRemoteException
     *             if no remote branch name <branch> exists
     */
    public static boolean callFetch(Repository repository, String branch) throws GitAPIException, IOException,
            NotRemoteException {
        Git git = new Git(repository);

        if (!hasRemoteBranch(repository, branch)) {
            throw new NotRemoteException("No remote branch '" + branch + "'. Cannot fetch it.");
        }

        if (hasLocalBranch(repository, branch)) {
            // git checkout <branch>
            git.checkout().setName(branch).call();

            // git pull --rebase origin
            git.pull().setRebase(true).setRemote("origin").call();

            return false;
        } else {
            git.fetch().setRefSpecs(new RefSpec("refs/heads/" + branch + ":refs/heads/" + branch)).setRemote("origin")
                    .call();
            git.checkout().setName(branch).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .setStartPoint("origin/" + branch).call();
            return true;
        }
    }

    public static String getShortName(Ref ref) {
        if (ref.getName().startsWith("refs/heads/")) {
            return ref.getName().substring("refs/heads/".length());
        } else if (ref.getName().startsWith("refs/remotes/origin/")) {
            return ref.getName().substring("refs/remotes/origin/".length());
        }
        return ref.getName();
    }

    public static List<String> listBranches(Repository repository, final String pattern) throws GitAPIException {
        Set<String> toReturn = new TreeSet<String>();

        Git git = new Git(repository);
        List<Ref> call = new ListBranchCommand(git.getRepository()).setListMode(ListMode.ALL).setPattern(pattern)
                .call();

        for (Ref ref : call) {
            toReturn.add(getShortName(ref));
        }

        return new ArrayList<String>(toReturn);
    }

    // TODO changes to junit
    public static void main(String[] args) throws GitAPIException, IOException {
        Git repo = Git.open(new File("/home/stephane/talend/checkouts/tac_save"));
        for (String ref : listBranches(repo.getRepository(), "master|maintenance/.*")) {
            System.out.println(ref);
        }
    }

    public static String getBugfixBranchName(String startPoint, String bugName) {
        String toReturn = GitConfiguration.getInstance().get("bugfixPrefix");
        toReturn += "/" + extractRootFromBranchName(startPoint);
        toReturn += "/" + bugName;
        return toReturn;
    }

    protected static String extractRootFromBranchName(String branchName) {
        if (branchName.startsWith(GitConfiguration.getInstance().get("maintenanceprefix"))) {
            return branchName.substring(GitConfiguration.getInstance().get("maintenanceprefix").length() + 1);
        } else {
            return branchName;
        }
    }

    /**
     * Merge (or rebase or squash) a branch on another. Used by FeatureFinish & BugfixFinish.
     * 
     * @param source
     *            the branch to rebase
     * @param target
     *            the branch to rebase on
     * @param branchType
     *            feature or bugfix (only used to display approriate message)
     * @param mergePolicy
     *            the policy used to merge (squash, rebase)
     */
    public static void merge(Writer writer, Git repo, String name, String sourcePrefix, String target,
            String branchType, MergePolicy mergePolicy, String command) throws IOException,
            InterruptedCommandException, GitAPIException, IncorrectRepositoryStateException {
        String source = getBugfixBranchName(target, name);
        if (branchType.equals("feature")) {
            source = sourcePrefix + "/" + name;
        }

        boolean hasRemote = GitUtils.hasRemote("origin", repo.getRepository());

        boolean continueAfterConflict = previouslyFinishingThisFeature(source, writer, command);

        // 1. Update sources from remote
        if (hasRemote) {
            try {
                GitUtils.callFetch(repo.getRepository(), target);
                GitUtils.callFetch(repo.getRepository(), source);
            } catch (NotRemoteException e) {
                // We don't care
            }
        }

        // 2. Test if such a branch exists
        if (!GitUtils.hasLocalBranch(repo.getRepository(), source)) {
            // TODO if continueAfterConflict==true, then we have an obsolete MERGE file, delete it

            IncorrectRepositoryStateException irse = new IncorrectRepositoryStateException("No local branch named '"
                    + source + "'");
            irse.addLine("To see " + branchType + " branches that may be finish, use:\n");
            irse.addLine(Strings.repeat(" ", Configuration.INSTANCE.getAsInt("geat.indentForCommandTemplates"))
                    + "git branch --list " + sourcePrefix + "/*");
            throw irse;
        }

        // 3. Try to rebase feature branch
        if (mergePolicy == MergePolicy.REBASE) {
            if (!continueAfterConflict) {
                // git checkout feature/myfeature
                repo.checkout().setName(source).call();
                // git rebase master
                RebaseResult rebaseResult = repo.rebase().setUpstream(target).call();

                if (rebaseResult.getStatus() == RebaseResult.Status.STOPPED) {
                    createMergeAbortedMarker(source, target, name, mergePolicy, command);
                }
            }

            // git checkout master
            repo.checkout().setName(target).call();
            // re-init featureBranchName because we just changed it:
            Ref ref = repo.getRepository().getRef(source);
            // git merge feature/myfeature
            repo.merge().setFastForward(FastForwardMode.FF_ONLY).include(ref).call();
        } else if (mergePolicy == MergePolicy.SQUASH) {
            if (!continueAfterConflict) {
                // git checkout master
                repo.checkout().setName(target).call();
                // git merge --squash feature/myfeature
                Ref ref = repo.getRepository().getRef(source);
                MergeResult mergeResult = repo.merge().setSquash(true).include(ref).call();

                if (mergeResult.getMergeStatus() == MergeStatus.CONFLICTING) {
                    createMergeAbortedMarker(source, target, name, mergePolicy, command);
                }

                String msg = InputsUtils.askUser("Commit message", "Finish " + branchType + " " + name);
                repo.commit().setMessage(msg).call();
            }
        }

        // 4. Remove feature branch
        repo.branchDelete().setBranchNames(source).setForce(mergePolicy == MergePolicy.SQUASH).call();

        writer.write("Summary of actions:");
        if (hasRemote) {
            writer.write(" - New commits from 'origin/" + source + "' has been pulled");
        }
        writer.write(" - The " + branchType + " branch '" + target + "' was rebased into '" + source + "'");
        writer.write(" - " + branchType + " branch '" + source + "' has been deleted");
        writer.write(" - You are now on branch '" + target + "'");
        if (hasRemote) {
            writer.write("");
            writer.write("Now, your new " + branchType + " is ready to be pushed. To do this, use:");
            writer.write("");
            writer.write(Strings.repeat(" ", Configuration.INSTANCE.getAsInt("geat.indentForCommandTemplates"))
                    + "git push origin " + target);
        }
    }

    /**
     * Check if a merge of this feature was started before this run.
     * 
     * @return true if was previously merging this feature, false otherwise
     * @throws InterruptedCommandException
     *             If was previously merging ANOTHER branch
     */
    private static boolean previouslyFinishingThisFeature(String featureName, Writer writer, String command)
            throws IOException, InterruptedCommandException {
        File mergeMarker = new File(getGeatConfigFolfer(), "MERGE");
        if (mergeMarker.exists()) {
            String readFirstLine = Files.readFirstLine(mergeMarker, Charsets.UTF_8);
            String[] split = readFirstLine.split(" ");
            if (split.length == 4 && split[0].equals("MERGE") && split[2].equals("IN")) {
                if (split[1].equals(featureName)) {
                    mergeMarker.delete();
                    return true;
                } else {
                    InterruptedCommandException ice = new InterruptedCommandException("Previous finish of " + split[1]
                            + " was aborted because of conflicts.");
                    ice.addLine("Please finish this feature first.");
                    ice.addLine("You can then complete the finish by running it again.\n");
                    ice.addLine(Strings.repeat(" ", Configuration.INSTANCE.getAsInt("geat.indentForCommandTemplates"))
                            + "geat " + command + " " + split[1] + " <policy>");
                    throw ice;
                }
            } else {
                // Malformed MERGE file:
                writer.write("WARN: previous file " + mergeMarker.getAbsolutePath() + " is malformed.");
                writer.write(Strings.repeat(" ", Configuration.INSTANCE.getAsInt("geat.indentForCommandTemplates"))
                        + readFirstLine);
                writer.write("Deleting the file");
                mergeMarker.delete();
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Creates a "merge in progress" file to indicates that a problems occurs during a partially done merge. This merge
     * must be finish before starts merging another issue.
     * 
     * Used when a conflicts occurs during a merge|rebase operation.
     */
    private static void createMergeAbortedMarker(String source, String target, String name, MergePolicy mergePolicy,
            String command) throws IOException, InterruptedCommandException {
        File mergeMarker = new File(getGeatConfigFolfer(), "MERGE");
        Files.createParentDirs(mergeMarker);
        Files.touch(mergeMarker);
        Files.write(("MERGE " + source + " IN " + target).getBytes(), mergeMarker);

        InterruptedCommandException ice = new InterruptedCommandException(
                "There were merge conflicts. To see more details, use:\n");
        ice.addLine(Strings.repeat(" ", Configuration.INSTANCE.getAsInt("geat.indentForCommandTemplates"))
                + "git status");
        ice.addLine("");
        ice.addLine("When resolved, you can then complete the finish by running it again.\n");
        ice.addLine(Strings.repeat(" ", Configuration.INSTANCE.getAsInt("geat.indentForCommandTemplates")) + "geat "
                + command + " " + name + " " + mergePolicy.toString().toLowerCase());
        throw ice;
    }

    private static File getGeatConfigFolfer() {
        File folder = new File(new File(System.getProperty("user.dir"), ".git"), ".geat");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }
}
