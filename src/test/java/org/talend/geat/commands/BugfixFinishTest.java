package org.talend.geat.commands;

import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.geat.JUnitUtils;
import org.talend.geat.exception.IllegalCommandArgumentException;

public class BugfixFinishTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testParseArgsOk1() throws IllegalCommandArgumentException, GitAPIException, IOException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        git.branchCreate().setName("bugfix/1.0/myBug").call();

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .setWorkingDir(git.getRepository().getDirectory().getParent())
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
                .setWorkingDir(git.getRepository().getDirectory().getParent())
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
                .setWorkingDir(git.getRepository().getDirectory().getParent())
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
                .setWorkingDir(git.getRepository().getDirectory().getParent())
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
                .setWorkingDir(git.getRepository().getDirectory().getParent())
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug", "2.0", "rebase" });
        Assert.assertEquals("myBug", command.featureName);
        Assert.assertEquals(MergePolicy.REBASE, command.mergePolicy);
        Assert.assertEquals("maintenance/2.0", command.target);
    }

    @Test
    public void testParseArgsError1() throws IllegalCommandArgumentException, GitAPIException, IOException {
        thrown.expect(IllegalCommandArgumentException.class);
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        git.branchCreate().setName("bugfix/1.0/myBug").call();
        git.branchCreate().setName("bugfix/2.0/myBug").call();

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .setWorkingDir(git.getRepository().getDirectory().getParent())
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
                .setWorkingDir(git.getRepository().getDirectory().getParent())
                .parseArgs(new String[] { BugfixFinish.NAME, "myBug", "rebase" });
    }

    @Test
    public void testParseArgsError3() throws IllegalCommandArgumentException, GitAPIException, IOException {
        thrown.expect(IllegalCommandArgumentException.class);
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");

        BugfixFinish command = (BugfixFinish) CommandsRegistry.INSTANCE.getCommand(BugfixFinish.NAME)
                .setWorkingDir(git.getRepository().getDirectory().getParent())
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

}
