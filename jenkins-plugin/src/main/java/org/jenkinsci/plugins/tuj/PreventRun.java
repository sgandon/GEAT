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
import hudson.model.Action;
import hudson.model.Queue.Item;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Executor;
import hudson.model.Project;
import hudson.model.Queue;
import hudson.model.Queue.Executable;
import hudson.model.Queue.QueueDecisionHandler;
import hudson.model.Queue.Task;
import hudson.tasks.BuildWrapperDescriptor;

import java.util.List;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

/**
 * created by sgandon on 6 juin 2014 Detailled comment
 * 
 */
@Extension
public class PreventRun extends QueueDecisionHandler {

    protected final static Logger log = Logger.getLogger(PreventRun.class.getName());

    /*
     * (non-Javadoc)
     * 
     * @see hudson.model.Queue.QueueDecisionHandler#shouldSchedule(hudson.model.Queue.Task, java.util.List)
     */
    @Override
    public boolean shouldSchedule(Task task, List<Action> actions) {
        if (!(task instanceof Project)) {
            return true;
        }

        final Project<?, ?> project = (Project<?, ?>) task;
        String[] keysFromProject = getKeysFromProject(project);
        if (keysFromProject.length != 0) {
            boolean isAnExclusiveBuildAlreadyRunning = areCurrentBuildsExclusiveFrom(keysFromProject);
            if (isAnExclusiveBuildAlreadyRunning) {
                log.info("Build of [" + project.getName() + "] was not scheduled because an exclusive build is already running.\n"); //$NON-NLS-1$//$NON-NLS-2$
            }
            return !isAnExclusiveBuildAlreadyRunning;
        }// else not exclusive policy so schedule
        return true;

    }

    /**
     * DOC sgandon Comment method "areCurrentBuildsExclusive".
     * 
     * @param keys
     * @return
     */
    @SuppressWarnings("rawtypes")
    boolean areCurrentBuildsExclusiveFrom(String[] keys) {
        Item[] items = getQueueItems();
        if (items.length > 0) {

            for (Item item : items) {
                if (item.task instanceof Project) {
                    String[] currentQueueKeys = getKeysFromProject((Project) item.task);
                    boolean isMatchingAExistingProject = isAnyKeyMatching(keys, currentQueueKeys);
                    if (isMatchingAExistingProject) {// break is a match is found
                        return true;
                    }// else no match so keep looking
                }
            }
        }
        // check with the current builds
        List<Executor> executors = getCurrentJobExecutors();
        for (Executor executor : executors) {
            Executable currentExecutable = executor.getCurrentExecutable();
            if (currentExecutable != null && currentExecutable instanceof AbstractBuild) {
                AbstractProject absProject = ((AbstractBuild) currentExecutable).getProject();
                if (absProject != null && absProject instanceof Project) {
                    String[] keysFromCurrentBuild = getKeysFromProject((Project) absProject);
                    boolean isMatchingAExistingProject = isAnyKeyMatching(keys, keysFromCurrentBuild);
                    if (isMatchingAExistingProject) {// break is a match is found
                        return true;
                    }// else no match so keep looking
                }
            }// else not found project to it is ok.
        }
        return false;
    }

    /**
     * DOC sgandon Comment method "getCurrentJobExecutors".
     * 
     * @return
     */
    List<Executor> getCurrentJobExecutors() {
        return Computer.currentComputer().getExecutors();
    }

    /**
     * DOC sgandon Comment method "getQueueItems".
     * 
     * @return
     */
    @Nonnull
    Item[] getQueueItems() {
        Queue queue = Queue.getInstance();
        return queue.getItems();
    }

    /**
     * return true if any key in keys is equals to one key in KeysToCompare
     * 
     * @param keys
     * @param currentQueueKeys
     * @return
     */
    boolean isAnyKeyMatching(@Nonnull
    String[] keys, @Nonnull
    String[] keysToCompare) {
        for (String key : keys) {
            for (String keyToCompare : keysToCompare) {
                if (key.equals(keyToCompare)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nonnull
    String[] getKeysFromProject(Project<?, ?> project) {
        ExclusiveKeyList exclusiveList = (ExclusiveKeyList) project.getBuildWrappers().get(getExclusiveKeyListDescriptor());
        if (exclusiveList != null) {
            String keys = exclusiveList.getKeys();
            return keys.trim().split(";"); //$NON-NLS-1$
        } else {
            return new String[0];
        }
    }

    Descriptor getExclusiveKeyListDescriptor() {
        return BuildWrapperDescriptor.find(ExclusiveKeyList.class.getName());
    }
}
