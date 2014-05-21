package org.talend.geat.commands;

/**
 * Defines all available merge policies when 'merging' a branch on another.
 */
public enum MergePolicy {
    REBASE, // All commits from source branch will be redo on target branch
    SQUASH; // Commits from source branch will be squashed to a single commit on target branch
}
