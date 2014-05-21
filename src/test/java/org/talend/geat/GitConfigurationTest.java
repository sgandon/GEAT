package org.talend.geat;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.io.Files;

public class GitConfigurationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // @Test
    public void testNonInit() throws GitAPIException {
        thrown.expect(IllegalStateException.class);

        File tempDir = Files.createTempDir();

        System.setProperty("user.dir", tempDir.getAbsolutePath());

        Assert.assertNull(GitConfiguration.getInstance().get("test"));
    }

    @Test
    public void testInit() throws IOException, GitAPIException {
        File tempDir = Files.createTempDir();

        System.setProperty("user.dir", tempDir.getAbsolutePath());

        Git.init().setDirectory(tempDir).call();

        Assert.assertNull(GitConfiguration.getInstance().get("test"));
        Assert.assertEquals("squash", GitConfiguration.getInstance().get("finishmergemode"));
    }

}
