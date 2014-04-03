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

public class ConfigurationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // @Test
    public void testNonInit() throws GitAPIException {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Configuration not set");

        File tempDir = Files.createTempDir();

        Git.init().setDirectory(tempDir).call();
        Configuration.getInstance().get("test");
    }

    @Test
    public void testInit() throws IOException, GitAPIException {
        File tempDir = Files.createTempDir();

        Git.init().setDirectory(tempDir).call();
        Configuration.setInstance(tempDir.getAbsolutePath());

        Assert.assertNull(Configuration.getInstance().get("test"));
        Assert.assertEquals("squash", Configuration.getInstance().get("finishmergemode"));
    }

}
