package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Test;
import org.talend.geat.GitUtils;
import org.talend.geat.JUnitUtils;
import org.talend.geat.exception.IllegalCommandArgumentException;
import org.talend.geat.exception.IncorrectRepositoryStateException;
import org.talend.geat.exception.InterruptedCommandException;

import com.google.common.io.Files;

public class FeatureFinishTest {

    @Test
    public void testParseArgs1() throws IllegalCommandArgumentException, GitAPIException, IOException {
        FeatureFinish command = (FeatureFinish) createCommandInstance().parseArgs(
                new String[] { "feature-finish", "myFeature", "rebase" });
        Assert.assertEquals("myFeature", command.featureName);
        Assert.assertEquals(MergePolicy.REBASE, command.mergePolicy);
    }

    @Test
    public void testParseArgs2() throws IllegalCommandArgumentException, GitAPIException, IOException {
        FeatureFinish command = (FeatureFinish) createCommandInstance().parseArgs(
                new String[] { "feature-finish", "myFeature", "squash" });
        Assert.assertEquals("myFeature", command.featureName);
        Assert.assertEquals(MergePolicy.SQUASH, command.mergePolicy);
    }

    @Test
    public void testParseArgsDefaultMergePolicy() throws GitAPIException, IOException, IllegalCommandArgumentException {
        FeatureFinish command = createCommandInstance();

        command.parseArgs(new String[] { "feature-finish", "myFeature" });
        Assert.assertEquals("myFeature", command.featureName);
        Assert.assertEquals(MergePolicy.SQUASH, command.mergePolicy);

    }

    @Test
    public void testExecute() throws GitAPIException, IOException, IncorrectRepositoryStateException,
            InterruptedCommandException {
        // Prepare:
        Git git = JUnitUtils.createTempRepo();

        File file1 = JUnitUtils.createInitialCommit(git, "file1");

        git.branchCreate().setName("feature/feature1").call();
        File file4 = JUnitUtils.createInitialCommit(git, "file4");
        git.checkout().setName("feature/feature1").call();
        File file2 = JUnitUtils.createInitialCommit(git, "file2");
        File file3 = JUnitUtils.createInitialCommit(git, "file3");

        // Test prepare:
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertFalse(file4.exists());

        git.checkout().setName("master").call();
        Assert.assertTrue(file1.exists());
        Assert.assertFalse(file2.exists());
        Assert.assertFalse(file3.exists());
        Assert.assertTrue(file4.exists());

        // Call our command:
        FeatureFinish command = new FeatureFinish();
        command.setFeatureName("feature1");
        command.setMergePolicy(MergePolicy.REBASE);
        command.run();

        // Test after:
        git.checkout().setName("master").call();
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertTrue(file4.exists());
        Assert.assertFalse(GitUtils.hasLocalBranch(git.getRepository(), "feature/feature1"));
    }

    @Test
    public void testExecuteRemote() throws GitAPIException, IOException, IncorrectRepositoryStateException,
            InterruptedCommandException {
        // Prepare:
        Git remote = JUnitUtils.createTempRepo();

        File file1 = JUnitUtils.createInitialCommit(remote, "file1");

        remote.branchCreate().setName("feature/feature1").call();
        File file4 = JUnitUtils.createInitialCommit(remote, "file4");
        remote.checkout().setName("feature/feature1").call();
        File file2 = JUnitUtils.createInitialCommit(remote, "file2");
        File file3 = JUnitUtils.createInitialCommit(remote, "file3");

        File tempDir = Files.createTempDir();
        Git.cloneRepository().setDirectory(tempDir).setRemote("origin")
                .setURI(remote.getRepository().getDirectory().getParentFile().getAbsolutePath()).call();
        Git git = Git.open(tempDir);

        System.setProperty("user.dir", tempDir.getAbsolutePath());

        file1 = new File(tempDir, "file1");
        file2 = new File(tempDir, "file2");
        file3 = new File(tempDir, "file3");
        file4 = new File(tempDir, "file4");

        // Test prepare:
        git.checkout().setName("feature/feature1").call();
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertFalse(file4.exists());

        git.checkout().setName("remotes/origin/master").call();
        Assert.assertTrue(file1.exists());
        Assert.assertFalse(file2.exists());
        Assert.assertFalse(file3.exists());
        Assert.assertTrue(file4.exists());

        // Call our command:
        remote.checkout().setName("master").call();
        FeatureFinish command = new FeatureFinish();
        command.setFeatureName("feature1");
        command.setMergePolicy(MergePolicy.REBASE);
        command.run();

        // Test after:
        git.checkout().setName("master").call();
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertTrue(file4.exists());
        Assert.assertFalse(GitUtils.hasLocalBranch(git.getRepository(), "feature/feature1"));
        Assert.assertFalse(GitUtils.hasRemoteBranch(git.getRepository(), "feature/feature1"));
    }

    private FeatureFinish createCommandInstance() throws GitAPIException, IOException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        return (FeatureFinish) CommandsRegistry.INSTANCE.getCommand(FeatureFinish.NAME);
    }
}
