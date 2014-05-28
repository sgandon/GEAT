package org.talend.geat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class InputsUtilsTest {

    @Test
    public void testAskUserChoicesOneChoice() {
        List<String> choices = new ArrayList<String>();
        choices.add("a");

        Assert.assertEquals("a", InputsUtils.askUser("ah", choices, "tagada"));
    }

}
