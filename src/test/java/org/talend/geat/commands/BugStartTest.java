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

public class BugStartTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testParseArgsOk() throws IllegalCommandArgumentException {
        BugStart command = (BugStart) CommandsRegistry.INSTANCE.getCommand(BugStart.NAME).parseArgs(
                new String[] { BugStart.NAME, "myBug" });
        Assert.assertEquals("myBug", command.bugName);
    }

    @Test
    public void testParseArgsWrongNumberArgs1() throws IllegalCommandArgumentException {
        thrown.expect(IllegalCommandArgumentException.class);
        CommandsRegistry.INSTANCE.getCommand(BugStart.NAME).parseArgs(
                new String[] { BugStart.NAME, "myBug", "anotherParam" });
    }

    @Test
    public void testParseArgsWrongNumberArgs2() throws IllegalCommandArgumentException {
        thrown.expect(IllegalCommandArgumentException.class);
        CommandsRegistry.INSTANCE.getCommand(BugStart.NAME).parseArgs(new String[] { BugStart.NAME });
    }

    @Test
    public void testExecuteBasic() throws GitAPIException, IOException, IllegalCommandArgumentException,
            IncorrectRepositoryStateException, InterruptedCommandException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        Assert.assertFalse(GitUtils.hasLocalBranch(git.getRepository(), "bug/tagada"));
        CommandsRegistry.INSTANCE.getCommand(BugStart.NAME).parseArgs(new String[] { BugStart.NAME, "tagada" })
                .setWorkingDir(git.getRepository().getDirectory().getParent()).setWriter(new DoNothingWriter()).run();
        Assert.assertTrue(GitUtils.hasLocalBranch(git.getRepository(), "bug/tagada"));
    }

    @Test
    public void testExecuteBranchAlreadyExist() throws GitAPIException, IOException, IllegalCommandArgumentException,
            IncorrectRepositoryStateException, InterruptedCommandException {
        thrown.expect(IncorrectRepositoryStateException.class);
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        Assert.assertFalse(GitUtils.hasLocalBranch(git.getRepository(), "bug/tagada"));

        git.branchCreate().setName("bug/tagada").call();
        Assert.assertTrue(GitUtils.hasLocalBranch(git.getRepository(), "bug/tagada"));

        CommandsRegistry.INSTANCE.getCommand(BugStart.NAME).parseArgs(new String[] { BugStart.NAME, "tagada" })
                .setWorkingDir(git.getRepository().getDirectory().getParent()).setWriter(new DoNothingWriter()).run();
    }

}
