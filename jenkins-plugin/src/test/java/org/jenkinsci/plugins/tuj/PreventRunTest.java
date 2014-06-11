// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.jenkinsci.plugins.tuj;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import hudson.model.FreeStyleBuild;
import hudson.model.ItemGroup;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Executor;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.tasks.BuildWrapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.plugins.tuj.ExclusiveKeyList.DescriptorImpl;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.reactor.ReactorException;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

/**
 * created by sgandon on 10 juin 2014 Detailled comment
 * 
 */

public class PreventRunTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    /**
     * 
     */
    private static final DescriptorImpl ExclusiveKeyListDescriptor = new ExclusiveKeyList.DescriptorImpl();

    @Test
    public void isAnyKeyMatchingTest() {
        PreventRun preventRun = createPreventRunSpy();
        assertFalse(preventRun.isAnyKeyMatching(new String[] { "aa", "bb" }, new String[] { "cc" })); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        assertFalse(preventRun.isAnyKeyMatching(new String[] { "bb" }, new String[] { "cc" }));//$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(preventRun.isAnyKeyMatching(new String[] { "aa", "bb" }, new String[] { "cc,dd" }));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertFalse(preventRun.isAnyKeyMatching(new String[] { "aa", "bb" }, new String[] {}));//$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(preventRun.isAnyKeyMatching(new String[] { "aa", "bb" }, new String[] { "aa" }));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
        assertTrue(preventRun.isAnyKeyMatching(new String[] { "aa", "bb" }, new String[] { "bb" }));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
        assertTrue(preventRun.isAnyKeyMatching(new String[] { "aa", "bb" }, new String[] { "aa", "bb" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$   
        assertTrue(preventRun.isAnyKeyMatching(new String[] { "bb" }, new String[] { "bb" })); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void getKeysFromProject() {
        PreventRun preventRun = createPreventRunSpy();
        Project<?, ?> project = createProjectWithKeySpy("foo"); //$NON-NLS-1$
        String[] keysFromProject = preventRun.getKeysFromProject(project);
        assertNotNull(keysFromProject);
        assertEquals(1, keysFromProject.length);
        assertEquals("foo", keysFromProject[0]);
    }

    @Test
    public void areCurrentBuildsExclusiveFromTest() throws NoSuchFieldException, SecurityException, Exception {
        PreventRun prs = createPreventRunSpy();
        assertFalse(prs.areCurrentBuildsExclusiveFrom(new String[] { "foo" })); //$NON-NLS-1$
        prs = createPreventRunSpy();
        // test queue test
        // failed to add a queue test
        // prs = createPreventRunSpy();
        // Item item = new
        // Whitebox.setInternalState(createProjectWithKeySpy("foo"), "task", qi);
        // doReturn(Collections.singletonList(qi)).when(prs).getQueueItems();
        //        assertTrue(prs.areCurrentBuildsExclusiveFrom(new String[] { "foo" })); //$NON-NLS-1$
        // prs = createPreventRunSpy();

        // test currentExecutor test
        prs = createPreventRunSpy();
        Executor executor = createExecutor("foo");
        doReturn(Collections.singletonList(executor)).when(prs).getCurrentJobExecutors();
        assertTrue(prs.areCurrentBuildsExclusiveFrom(new String[] { "foo" })); //$NON-NLS-1$

    }

    /**
     * DOC sgandon Comment method "createExecutor".
     * 
     * @param key
     * 
     * @return
     * @throws ReactorException
     * @throws InterruptedException
     * @throws IOException
     */
    private Executor createExecutor(String key) throws IOException, InterruptedException, ReactorException {
        FreeStyleProject freeStyleProject = createProjectWithKeySpy(key);
        FreeStyleBuild freeStyleBuildMock = Mockito.mock(FreeStyleBuild.class, Mockito.CALLS_REAL_METHODS);
        doReturn(freeStyleProject).when(freeStyleBuildMock).getProject();
        Executor exec = spy(new Executor(Computer.currentComputer(), 0));
        doReturn(freeStyleBuildMock).when(exec).getCurrentExecutable();
        return exec;
    }

    /**
     * DOC sgandon Comment method "createPreventRun".
     * 
     * @return
     */
    private PreventRun createPreventRunSpy() {
        PreventRun pv = spy(new PreventRun());
        doReturn(ExclusiveKeyListDescriptor).when(pv).getExclusiveKeyListDescriptor();
        return pv;
    }

    /**
     * DOC sgandon Comment method "createProjectWithKey".
     * 
     * @param string
     */

    private FreeStyleProject createProjectWithKeySpy(String key) {
        @SuppressWarnings("rawtypes")
        FreeStyleProject fp = spy(new FreeStyleProject((ItemGroup) null, "TheProject")); //$NON-NLS-1$
        @SuppressWarnings("unused")
        Map<Descriptor<BuildWrapper>, BuildWrapper> buildWrappers = new HashMap<Descriptor<BuildWrapper>, BuildWrapper>();
        buildWrappers.put(ExclusiveKeyListDescriptor, new ExclusiveKeyList(true, key));
        doReturn(buildWrappers).when(fp).getBuildWrappers();
        return fp;

    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers"); //$NON-NLS-1$
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
