package org.talend.geat.commands;

import org.junit.Assert;
import org.junit.Test;


public class BugfixFinishTest {

    @Test
    public void testExtractStartpointFromBugName() {
        BugfixFinish com = new BugfixFinish();
        Assert.assertEquals("master", com.extractStartpointFromBugName("bugfix/master/TDI-12000"));
        Assert.assertEquals("maintenance/5.4", com.extractStartpointFromBugName("bugfix/5.4/TDI-12000"));
        Assert.assertEquals("release/5.4.2", com.extractStartpointFromBugName("bugfix/5.4.2/TDI-12000"));
    }

}
