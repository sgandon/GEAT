package org.talend.geat;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RemoteConfig;

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

    public static boolean hasRemoteBranch(Repository repository, String branch) throws InvalidRemoteException,
            TransportException, GitAPIException {
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
     * @throws GitAPIException
     * @throws CheckoutConflictException
     * @throws InvalidRefNameException
     * @throws RefNotFoundException
     * @throws RefAlreadyExistsException
     */
    public static boolean callFetch(Repository repository, String branch) throws RefAlreadyExistsException,
            RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException {
        Git git = new Git(repository);
        if (hasRemoteBranch(repository, branch)) {

            // git checkout master
            git.checkout().setName(branch).call();

            // git pull --rebase origin
            git.pull().setRebase(true).setRemote("origin").call();

            return true;
        } else {
            return false;
        }
    }
}
