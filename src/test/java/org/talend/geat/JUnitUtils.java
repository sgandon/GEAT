package org.talend.geat;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.common.io.Files;

public class JUnitUtils {

    public static Git createTempRepo() throws GitAPIException, IOException {
        File tempDir = Files.createTempDir();
        System.setProperty("user.dir", tempDir.getAbsolutePath());
        Git.init().setDirectory(tempDir).call();
        Git git = Git.open(tempDir);
        return git;
    }

    public static File createInitialCommit(Git git, String fileName) throws IOException, GitAPIException {
        File aFile = new File(git.getRepository().getDirectory().getAbsoluteFile().getParentFile(), fileName);
        aFile.createNewFile();
        git.add().addFilepattern(fileName).call();
        git.commit().setMessage("Initial commit (add " + fileName + ")").call();
        return aFile;
    }
}
