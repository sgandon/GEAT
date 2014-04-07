package org.talend.geat.commands;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.exception.IllegalCommandArgumentException;
import org.talend.geat.exception.IncorrectRepositoryStateException;
import org.talend.geat.exception.InterruptedCommandException;
import org.talend.geat.io.DoNothingWriter;

import com.google.common.io.Files;
import com.google.common.io.LineReader;

public class CommandTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private class DoNothingCommand extends Command {

        private boolean hasRun  = false;

        private String  aString = "default";

        @Override
        public String getCommandName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getUsage() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getDescription() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected void execute(Writer writer) throws IncorrectRepositoryStateException, IOException, GitAPIException,
                InterruptedCommandException {
            hasRun = true;
            writer.write("my test output");
        }

        @Override
        public Command parseArgs(String[] args) throws IllegalCommandArgumentException {
            aString = args[0];
            return this;
        }

        @Override
        public CheckLevel getCheckLevel() {
            return CheckLevel.GIT_REPO_ONLY;
        }

    }

    private DoNothingCommand getDoNothingCommandInstance(boolean gitInit) throws GitAPIException {
        final File tempDir = Files.createTempDir();
        if (gitInit) {
            Git.init().setDirectory(tempDir).call();
        }
        return (DoNothingCommand) new DoNothingCommand().setWorkingDir(tempDir.getAbsolutePath()).setWriter(
                new DoNothingWriter());
    }

    @Test
    public void testCallableOnce() throws IncorrectRepositoryStateException, IOException, GitAPIException,
            InterruptedCommandException {
        thrown.expect(IllegalStateException.class);
        DoNothingCommand com = getDoNothingCommandInstance(true);
        try {
            com.run();
        } catch (IllegalStateException e) {
            Assert.fail("First run should succeed");
        }
        com.run();
    }

    /**
     * Simply test that run() calls execute()
     */
    @Test
    public void testRun1() throws IncorrectRepositoryStateException, IOException, GitAPIException,
            InterruptedCommandException {
        DoNothingCommand com = getDoNothingCommandInstance(true);
        Assert.assertFalse(com.hasRun);
        com.run();
        Assert.assertTrue(com.hasRun);
    }

    /**
     * Test that sanity check throw exception BEFORE call execute()
     */
    @Test
    public void testRunSanity() throws IncorrectRepositoryStateException, IOException, GitAPIException,
            InterruptedCommandException {
        DoNothingCommand com = getDoNothingCommandInstance(false);
        Assert.assertFalse(com.hasRun);
        try {
            com.run();
            Assert.fail("IncorrectRepositoryStateException should have been raised here");
        } catch (IncorrectRepositoryStateException e) {
            Assert.assertFalse(com.hasRun);
        }
    }

    @Test
    public void testWriter() throws GitAPIException, IOException, IncorrectRepositoryStateException,
            InterruptedCommandException {
        DoNothingCommand com = getDoNothingCommandInstance(true);

        File outputFile = new File(Files.createTempDir(), "output.txt");
        Writer writer = new FileWriter(outputFile);

        com.setWriter(writer).run();
        writer.close();

        LineReader lineReader = new LineReader(new FileReader(outputFile));
        Assert.assertEquals("my test output", lineReader.readLine());
    }

    @Test
    public void testParseArgs() throws GitAPIException, IOException, IncorrectRepositoryStateException,
            InterruptedCommandException, IllegalCommandArgumentException {
        DoNothingCommand com = getDoNothingCommandInstance(true);
        Assert.assertEquals("default", com.aString);
        com.parseArgs(new String[] { "real" }).run();
        Assert.assertEquals("real", com.aString);
    }
}
