package org.talend.geat.commands;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.geat.GitUtils;
import org.talend.geat.JUnitUtils;
import org.talend.geat.exception.IllegalCommandArgumentException;
import org.talend.geat.exception.IncorrectRepositoryStateException;
import org.talend.geat.exception.InterruptedCommandException;

import com.google.common.io.Files;

public class BugfixFinishTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testParseArgsOk1() throws IllegalCommandArgumentException, GitAPIException, IOException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        git.branchCreate().setName("bugfix/1.0/myBug").call();

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug" });
        Assert.assertEquals("myBug", command.featureName);
        Assert.assertEquals(MergePolicy.SQUASH, command.mergePolicy);
        Assert.assertEquals("maintenance/1.0", command.target);
    }

    @Test
    public void testParseArgsOk2() throws IllegalCommandArgumentException, GitAPIException, IOException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        git.branchCreate().setName("bugfix/1.0/myBug").call();

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug", "rebase" });
        Assert.assertEquals("myBug", command.featureName);
        Assert.assertEquals(MergePolicy.REBASE, command.mergePolicy);
        Assert.assertEquals("maintenance/1.0", command.target);
    }

    @Test
    public void testParseArgsOk3() throws IllegalCommandArgumentException, GitAPIException, IOException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        git.branchCreate().setName("bugfix/1.0/myBug").call();

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug", "squash" });
        Assert.assertEquals("myBug", command.featureName);
        Assert.assertEquals(MergePolicy.SQUASH, command.mergePolicy);
        Assert.assertEquals("maintenance/1.0", command.target);
    }

    @Test
    public void testParseArgsOk4() throws IllegalCommandArgumentException, GitAPIException, IOException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        git.branchCreate().setName("bugfix/1.0/myBug").call();
        git.branchCreate().setName("bugfix/2.0/myBug").call();

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug", "2.0" });
        Assert.assertEquals("myBug", command.featureName);
        Assert.assertEquals(MergePolicy.SQUASH, command.mergePolicy);
        Assert.assertEquals("maintenance/2.0", command.target);
    }

    @Test
    public void testParseArgsOk5() throws IllegalCommandArgumentException, GitAPIException, IOException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        git.branchCreate().setName("bugfix/1.0/myBug").call();
        git.branchCreate().setName("bugfix/2.0/myBug").call();

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug", "2.0", "rebase" });
        Assert.assertEquals("myBug", command.featureName);
        Assert.assertEquals(MergePolicy.REBASE, command.mergePolicy);
        Assert.assertEquals("maintenance/2.0", command.target);
    }

    @Test
    public void testParseArgsOkMaster1() throws IllegalCommandArgumentException, GitAPIException, IOException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        git.branchCreate().setName("bugfix/master/myBug").call();
        git.branchCreate().setName("bugfix/1.0/myBug").call();

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug", "master", "rebase" });
        Assert.assertEquals("myBug", command.featureName);
        Assert.assertEquals(MergePolicy.REBASE, command.mergePolicy);
        Assert.assertEquals("master", command.target);
    }

    @Test
    public void testParseArgsOkMaster2() throws IllegalCommandArgumentException, GitAPIException, IOException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        git.branchCreate().setName("bugfix/master/myBug").call();

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug", "rebase" });
        Assert.assertEquals("myBug", command.featureName);
        Assert.assertEquals(MergePolicy.REBASE, command.mergePolicy);
        Assert.assertEquals("master", command.target);
    }

    @Test
    public void testParseArgsError1() throws IllegalCommandArgumentException, GitAPIException, IOException {
        thrown.expect(IllegalCommandArgumentException.class);
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        git.branchCreate().setName("bugfix/1.0/myBug").call();
        git.branchCreate().setName("bugfix/2.0/myBug").call();

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug" });
    }

    @Test
    public void testParseArgsError2() throws IllegalCommandArgumentException, GitAPIException, IOException {
        thrown.expect(IllegalCommandArgumentException.class);
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        git.branchCreate().setName("bugfix/1.0/myBug").call();
        git.branchCreate().setName("bugfix/2.0/myBug").call();

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug", "rebase" });
    }

    @Test
    public void testParseArgsError3() throws IllegalCommandArgumentException, GitAPIException, IOException {
        thrown.expect(IllegalCommandArgumentException.class);
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug", "rebase" });
    }

    @Test
    public void testParseArgsError5() throws IllegalCommandArgumentException, GitAPIException, IOException {
        thrown.expect(IllegalCommandArgumentException.class);
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME).parseArgs(
                new String[] { BugfixFinish.NAME, "myBug" });
    }

    @Test
    public void testExtractStartpointFromBugName() throws IllegalCommandArgumentException {
        BugfixFinish com = new BugfixFinish();
        Assert.assertEquals("master", com.extractStartpointFromBugName("bugfix/master/TDI-12000"));
        Assert.assertEquals("maintenance/5.4", com.extractStartpointFromBugName("bugfix/5.4/TDI-12000"));
        Assert.assertEquals("release/5.4.2", com.extractStartpointFromBugName("bugfix/5.4.2/TDI-12000"));
    }

    @Test
    public void testExtractStartpointFromBugNameError() throws IllegalCommandArgumentException {
        thrown.expect(IllegalCommandArgumentException.class);
        BugfixFinish com = new BugfixFinish();
        com.extractStartpointFromBugName("TDI-12000");
    }

    @Test
    public void testExecuteBasicMaster() throws GitAPIException, IOException, IncorrectRepositoryStateException,
            InterruptedCommandException {
        // Prepare:
        Git git = JUnitUtils.createTempRepo();

        File file1 = JUnitUtils.createInitialCommit(git, "file1");

        git.branchCreate().setName("bugfix/master/myBug").call();
        File file4 = JUnitUtils.createInitialCommit(git, "file4");
        git.checkout().setName("bugfix/master/myBug").call();
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
        BugfixFinish command = new BugfixFinish();
        command.setFeatureName("myBug");
        command.setMergePolicy(MergePolicy.REBASE);
        command.setTarget("master");
        command.run();

        // Test after:
        git.checkout().setName("master").call();
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertTrue(file4.exists());
        Assert.assertFalse(GitUtils.hasLocalBranch(git.getRepository(), "bugfix/master/myBug"));
    }

    @Test
    public void testExecuteRemote() throws GitAPIException, IOException, IncorrectRepositoryStateException,
            InterruptedCommandException {
        // Prepare:
        Git remote = JUnitUtils.createTempRepo();

        File file1 = JUnitUtils.createInitialCommit(remote, "file1");

        remote.branchCreate().setName("bugfix/master/myBug").call();
        File file4 = JUnitUtils.createInitialCommit(remote, "file4");
        remote.checkout().setName("bugfix/master/myBug").call();
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
        git.checkout().setName("bugfix/master/myBug").call();
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
        BugfixFinish command = new BugfixFinish();
        command.setFeatureName("myBug");
        command.setMergePolicy(MergePolicy.REBASE);
        command.setTarget("master");
        command.run();

        // Test after:
        git.checkout().setName("master").call();
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertTrue(file4.exists());
        Assert.assertFalse(GitUtils.hasLocalBranch(git.getRepository(), "bugfix/master/myBug"));
        Assert.assertFalse(GitUtils.hasRemoteBranch(git.getRepository(), "bugfix/master/myBug"));
    }

    @Test
    public void testExecuteBasicMaint() throws GitAPIException, IOException, IncorrectRepositoryStateException,
            InterruptedCommandException {
        // Prepare:
        Git git = JUnitUtils.createTempRepo();

        File file1 = JUnitUtils.createInitialCommit(git, "file1");

        git.branchCreate().setName("maintenance/1.0").call();
        File file2 = JUnitUtils.createInitialCommit(git, "file2");

        git.checkout().setName("maintenance/1.0").call();
        File file3 = JUnitUtils.createInitialCommit(git, "file3");

        git.branchCreate().setName("bugfix/1.0/myBug").call();
        git.checkout().setName("bugfix/1.0/myBug").call();
        File file4 = JUnitUtils.createInitialCommit(git, "file4");

        // Test prepare:
        git.checkout().setName("master").call();
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file2.exists());
        Assert.assertFalse(file3.exists());
        Assert.assertFalse(file4.exists());

        git.checkout().setName("maintenance/1.0").call();
        Assert.assertTrue(file1.exists());
        Assert.assertFalse(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertFalse(file4.exists());

        git.checkout().setName("bugfix/1.0/myBug").call();
        Assert.assertTrue(file1.exists());
        Assert.assertFalse(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertTrue(file4.exists());

        // Call our command:
        BugfixFinish command = new BugfixFinish();
        command.setFeatureName("myBug");
        command.setMergePolicy(MergePolicy.REBASE);
        command.setTarget("maintenance/1.0");
        command.run();

        // Test after:
        git.checkout().setName("master").call();
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file2.exists());
        Assert.assertFalse(file3.exists());
        Assert.assertFalse(file4.exists());
        git.checkout().setName("maintenance/1.0").call();
        Assert.assertTrue(file1.exists());
        Assert.assertFalse(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertTrue(file4.exists());
        Assert.assertFalse(GitUtils.hasLocalBranch(git.getRepository(), "bugfix/0.1/myBug"));
    }

    @Test
    public void testExecuteBasicRelease() throws GitAPIException, IOException, IncorrectRepositoryStateException,
            InterruptedCommandException {
        // Prepare:
        Git git = JUnitUtils.createTempRepo();

        File file1 = JUnitUtils.createInitialCommit(git, "file1");

        git.branchCreate().setName("release/5.4.2").call();
        File file2 = JUnitUtils.createInitialCommit(git, "file2");

        git.checkout().setName("release/5.4.2").call();
        File file3 = JUnitUtils.createInitialCommit(git, "file3");

        git.branchCreate().setName("bugfix/5.4.2/myBug").call();
        git.checkout().setName("bugfix/5.4.2/myBug").call();
        File file4 = JUnitUtils.createInitialCommit(git, "file4");

        // Test prepare:
        git.checkout().setName("master").call();
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file2.exists());
        Assert.assertFalse(file3.exists());
        Assert.assertFalse(file4.exists());

        git.checkout().setName("release/5.4.2").call();
        Assert.assertTrue(file1.exists());
        Assert.assertFalse(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertFalse(file4.exists());

        git.checkout().setName("bugfix/5.4.2/myBug").call();
        Assert.assertTrue(file1.exists());
        Assert.assertFalse(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertTrue(file4.exists());

        // Call our command:
        BugfixFinish command = new BugfixFinish();
        command.setFeatureName("myBug");
        command.setMergePolicy(MergePolicy.REBASE);
        command.setTarget("release/5.4.2");
        command.run();

        // Test after:
        git.checkout().setName("master").call();
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file2.exists());
        Assert.assertFalse(file3.exists());
        Assert.assertFalse(file4.exists());
        git.checkout().setName("release/5.4.2").call();
        Assert.assertTrue(file1.exists());
        Assert.assertFalse(file2.exists());
        Assert.assertTrue(file3.exists());
        Assert.assertTrue(file4.exists());
        Assert.assertFalse(GitUtils.hasLocalBranch(git.getRepository(), "bugfix/5.4.2/myBug"));
    }
}
