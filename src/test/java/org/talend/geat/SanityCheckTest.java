package org.talend.geat;


import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Test;
import org.talend.geat.SanityCheck.CheckLevel;

import com.google.common.io.Files;

public class SanityCheckTest {

    @Test
    public void testCheckBasic() {
        Assert.assertFalse(SanityCheck.check("/one/folder/that/should/not/exists", CheckLevel.GIT_REPO_ONLY, false,
                false));

        File tempDir = Files.createTempDir();
        Assert.assertFalse(SanityCheck.check(tempDir.getAbsolutePath(), CheckLevel.GIT_REPO_ONLY, false, false));

        try {
            Git.init().setDirectory(tempDir).call();
        } catch (GitAPIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Assert.assertTrue(SanityCheck.check(tempDir.getAbsolutePath(), CheckLevel.GIT_REPO_ONLY, false, false));
    }

    @Test
    public void testCheckUntracked() {
        File tempDir = Files.createTempDir();

        try {
            Git.init().setDirectory(tempDir).call();

            Git repo = Git.open(tempDir);
            File child = new File(tempDir, "child");
            child.createNewFile();
            repo.add().addFilepattern("child").call();

            Assert.assertFalse(SanityCheck.check(tempDir.getAbsolutePath(), CheckLevel.NO_UNCOMMITTED_CHANGES, false,
                    false));

            repo.commit().setMessage("Initial commit").call();
            Assert.assertTrue(SanityCheck.check(tempDir.getAbsolutePath(), CheckLevel.NO_UNCOMMITTED_CHANGES, false,
                    false));
        } catch (GitAPIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
