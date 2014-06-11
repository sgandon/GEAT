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

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Project;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * created by sgandon on 6 juin 2014 Detailled comment
 * 
 */
public class ExclusiveKeyList extends BuildWrapper {

    protected final static Logger log = Logger.getLogger(ExclusiveKeyList.class.getName());

    private String keys;

    /**
     * DOC sgandon ExclusiveKeyList constructor comment.
     */
    @DataBoundConstructor
    public ExclusiveKeyList(boolean enabled, String keys) {
        super();
        this.keys = keys;
        log.info("keys:" + keys);
        System.out.println("keys=" + keys);
    }

    /**
     * Getter for keys.
     * 
     * @return the keys
     */
    public String getKeys() {
        return keys;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hudson.tasks.BuildWrapper#setUp(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)
     */
    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
            InterruptedException {
        return new NoneEnvironment();
    }

    /**
     * Descriptor for {@link ExclusiveBuildWrapper}. Used as a singleton. The class is marked as public so that it can
     * be accessed from views.
     */
    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        @Override
        public String getDisplayName() {
            return "Exclusive key set";
        }

        @Override
        public boolean isApplicable(AbstractProject item) {
            return (item instanceof Project);
        }
    }

    /**
     * handles the post-build tasks such as canceling the quiet down sequencing
     * 
     */
    private class NoneEnvironment extends Environment {

        public NoneEnvironment() {
        }

        @Override
        public boolean tearDown(AbstractBuild build, BuildListener listener) {
            return true;
        }
    }
}
