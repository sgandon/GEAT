package org.talend.geat.commands;

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
import org.talend.geat.io.DoNothingWriter;

public class BugfixStartTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testParseArgsOk1() throws IllegalCommandArgumentException {
        BugfixStart command = (BugfixStart) CommandsRegistry.INSTANCE.getCommand(BugfixStart.NAME).parseArgs(
                new String[] { BugfixStart.NAME, "myBug" });
        Assert.assertEquals("myBug", command.bugName);
        Assert.assertEquals("master", command.startPoint);
    }
    @Test
    public void testParseArgsOk2() throws IllegalCommandArgumentException {
        BugfixStart command = (BugfixStart) CommandsRegistry.INSTANCE.getCommand(BugfixStart.NAME).parseArgs(
                new String[] { BugfixStart.NAME, "myBug", "startpoint" });
        Assert.assertEquals("myBug", command.bugName);
        Assert.assertEquals("startpoint", command.startPoint);
    }

    @Test
    public void testParseArgsWrongNumberArgs1() throws IllegalCommandArgumentException {
        thrown.expect(IllegalCommandArgumentException.class);
        CommandsRegistry.INSTANCE.getCommand(BugfixStart.NAME).parseArgs(
                new String[] { BugfixStart.NAME, "myBug", "anotherParam", "oneMoreParam" });
    }

    @Test
    public void testParseArgsWrongNumberArgs2() throws IllegalCommandArgumentException {
        thrown.expect(IllegalCommandArgumentException.class);
        CommandsRegistry.INSTANCE.getCommand(BugfixStart.NAME).parseArgs(new String[] { BugfixStart.NAME });
    }

    @Test
    public void testExecuteBasic() throws GitAPIException, IOException, IllegalCommandArgumentException,
            IncorrectRepositoryStateException, InterruptedCommandException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        Assert.assertFalse(GitUtils.hasLocalBranch(git.getRepository(), "bugfix/tagada"));
        CommandsRegistry.INSTANCE.getCommand(BugfixStart.NAME).parseArgs(new String[] { BugfixStart.NAME, "tagada" })
                .setWorkingDir(git.getRepository().getDirectory().getParent()).setWriter(new DoNothingWriter()).run();
        Assert.assertTrue(GitUtils.hasLocalBranch(git.getRepository(), "bugfix/tagada"));
    }

    @Test
    public void testExecuteBranchAlreadyExist() throws GitAPIException, IOException, IllegalCommandArgumentException,
            IncorrectRepositoryStateException, InterruptedCommandException {
        thrown.expect(IncorrectRepositoryStateException.class);
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        Assert.assertFalse(GitUtils.hasLocalBranch(git.getRepository(), "bugfix/tagada"));

        git.branchCreate().setName("bugfix/tagada").call();
        Assert.assertTrue(GitUtils.hasLocalBranch(git.getRepository(), "bugfix/tagada"));

        CommandsRegistry.INSTANCE.getCommand(BugfixStart.NAME).parseArgs(new String[] { BugfixStart.NAME, "tagada" })
                .setWorkingDir(git.getRepository().getDirectory().getParent()).setWriter(new DoNothingWriter()).run();
    }

}
