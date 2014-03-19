package org.talend.geat.commands;

import org.junit.Assert;
import org.junit.Test;

public class FeatureStartTest {

    @Test
    public void testParseArgs() {
        FeatureStart command = (FeatureStart) new FeatureStart()
                .parseArgs(new String[] { "feature-start", "myFeature" });
        Assert.assertEquals("myFeature", command.featureName);
    }

}
