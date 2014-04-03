package org.talend.geat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.talend.geat.exception.NotRemoteException;

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
}
