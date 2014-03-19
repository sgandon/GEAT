package org.talend.geat.commands;

import org.junit.Assert;
import org.junit.Test;
import org.talend.geat.commands.FeatureFinish.MergePolicy;

public class FeatureFinishTest {

    @Test
    public void testParseArgs1() {
        FeatureFinish command = (FeatureFinish) new FeatureFinish().parseArgs(new String[] { "feature-finish",
                "myFeature", "rebase" });
        Assert.assertEquals("myFeature", command.featureName);
        Assert.assertEquals(MergePolicy.REBASE, command.mergePolicy);
    }

    @Test
    public void testParseArgs2() {
        FeatureFinish command = (FeatureFinish) new FeatureFinish().parseArgs(new String[] { "feature-finish",
                "myFeature", "squash" });
        Assert.assertEquals("myFeature", command.featureName);
        Assert.assertEquals(MergePolicy.SQUASH, command.mergePolicy);
    }
}
