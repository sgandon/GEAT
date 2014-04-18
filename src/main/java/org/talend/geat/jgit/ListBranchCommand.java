package org.talend.geat.jgit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.talend.geat.GitUtils;

/**
 * Extends org.eclipse.jgit.api.ListBranchCommand to add pattern matches.
 * 
 * TODO try to commit in jGit
 */
public class ListBranchCommand extends org.eclipse.jgit.api.ListBranchCommand {

    private String pattern;

    public ListBranchCommand(Repository repo) {
        super(repo);
    }

    @Override
    public List<Ref> call() throws GitAPIException {
        List<Ref> unfiltered = super.call();

        List<Ref> toReturn = new ArrayList<Ref>();

        for (Ref ref : unfiltered) {
            if (GitUtils.getShortName(ref).matches(pattern)) {
                toReturn.add(ref);
            }
        }

        return toReturn;
    }

    public String getPattern() {
        return pattern;
    }

    public ListBranchCommand setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    @Override
    public ListBranchCommand setListMode(ListMode listMode) {
        super.setListMode(listMode);
        return this;
    }

}
