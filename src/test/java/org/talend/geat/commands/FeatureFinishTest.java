package org.talend.geat.commands;

import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Test;
import org.talend.geat.JUnitUtils;
import org.talend.geat.commands.FeatureFinish.MergePolicy;
import org.talend.geat.exception.IllegalCommandArgumentException;

public class FeatureFinishTest {

    @Test
    public void testParseArgs1() throws IllegalCommandArgumentException {
        FeatureFinish command = (FeatureFinish) CommandsRegistry.INSTANCE.getCommand(FeatureFinish.NAME).parseArgs(
                new String[] { "feature-finish", "myFeature", "rebase" });
        Assert.assertEquals("myFeature", command.featureName);
        Assert.assertEquals(MergePolicy.REBASE, command.mergePolicy);
    }

    @Test
    public void testParseArgs2() throws IllegalCommandArgumentException {
        FeatureFinish command = (FeatureFinish) CommandsRegistry.INSTANCE.getCommand(FeatureFinish.NAME).parseArgs(
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

    private FeatureFinish createCommandInstance() throws GitAPIException, IOException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        return (FeatureFinish) CommandsRegistry.INSTANCE.getCommand(FeatureFinish.NAME).setWorkingDir(
                git.getRepository().getDirectory().getParent());
    }
}
