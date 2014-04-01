package org.talend.geat.commands;

import org.junit.Assert;
import org.junit.Test;
import org.talend.geat.exception.IllegalCommandArgumentException;

public class FeatureStartTest {

    @Test
    public void testParseArgs() throws IllegalCommandArgumentException {
        FeatureStart command = (FeatureStart) new FeatureStart()
                .parseArgs(new String[] { "feature-start", "myFeature" });
        Assert.assertEquals("myFeature", command.featureName);
    }

}
