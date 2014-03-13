package org.talend.geat;

import java.net.URISyntaxException;
import java.util.List;

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

}
