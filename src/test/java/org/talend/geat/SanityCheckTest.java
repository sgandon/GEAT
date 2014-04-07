package org.talend.geat;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.exception.IncorrectRepositoryStateException;

import com.google.common.io.Files;

public class SanityCheckTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCheckNone1() throws GitAPIException, IncorrectRepositoryStateException {
        SanityCheck.check("/one/folder/that/should/not/exists", CheckLevel.NONE);
    }

    @Test
    public void testCheckNone2() throws GitAPIException, IncorrectRepositoryStateException {
        File tempDir = Files.createTempDir();
        SanityCheck.check(tempDir.getAbsolutePath(), CheckLevel.NONE);
    }

    @Test
    public void testCheckNone3() throws GitAPIException, IncorrectRepositoryStateException {
        File tempDir = Files.createTempDir();
        Git.init().setDirectory(tempDir).call();
        SanityCheck.check(tempDir.getAbsolutePath(), CheckLevel.NONE);
    }

    @Test
    public void testCheckGitRepoOnlyNonExistingFolder() throws IncorrectRepositoryStateException {
        thrown.expect(IncorrectRepositoryStateException.class);
        SanityCheck.check("/one/folder/that/should/not/exists", CheckLevel.GIT_REPO_ONLY);
    }

    @Test
    public void testCheckGitRepoOnlyExistingFolder() throws IncorrectRepositoryStateException {
        thrown.expect(IncorrectRepositoryStateException.class);
        File tempDir = Files.createTempDir();
        SanityCheck.check(tempDir.getAbsolutePath(), CheckLevel.GIT_REPO_ONLY);
    }

    @Test
    public void testCheckGitRepoOnlyGitRepo() throws GitAPIException, IncorrectRepositoryStateException {
        File tempDir = Files.createTempDir();
        Git.init().setDirectory(tempDir).call();
        SanityCheck.check(tempDir.getAbsolutePath(), CheckLevel.GIT_REPO_ONLY);
    }

    @Test
    public void testCheckUntracked1() throws GitAPIException, IOException, IncorrectRepositoryStateException {
        thrown.expect(IncorrectRepositoryStateException.class);
        File tempDir = Files.createTempDir();

        Git.init().setDirectory(tempDir).call();

        Git repo = Git.open(tempDir);
        File child = new File(tempDir, "child");
        child.createNewFile();
        repo.add().addFilepattern("child").call();

        SanityCheck.check(tempDir.getAbsolutePath(), CheckLevel.NO_UNCOMMITTED_CHANGES);
    }

    @Test
    public void testCheckUntracked2() throws GitAPIException, IOException, IncorrectRepositoryStateException {
        File tempDir = Files.createTempDir();

        Git.init().setDirectory(tempDir).call();

        Git repo = Git.open(tempDir);
        File child = new File(tempDir, "child");
        child.createNewFile();
        repo.add().addFilepattern("child").call();

        SanityCheck.check(tempDir.getAbsolutePath(), CheckLevel.GIT_REPO_ONLY);
    }

    @Test
    public void testCheckUntracked3() throws GitAPIException, IOException, IncorrectRepositoryStateException {
        File tempDir = Files.createTempDir();

        Git.init().setDirectory(tempDir).call();

        Git repo = Git.open(tempDir);
        File child = new File(tempDir, "child");
        child.createNewFile();
        repo.add().addFilepattern("child").call();

        repo.commit().setMessage("Initial commit").call();
        SanityCheck.check(tempDir.getAbsolutePath(), CheckLevel.NO_UNCOMMITTED_CHANGES);
    }

    @Test
    public void testCheckUntrackedWithUtils() throws GitAPIException, IOException, IncorrectRepositoryStateException {
        Git git = JUnitUtils.createTempRepo();
        JUnitUtils.createInitialCommit(git, "file1");
        SanityCheck.check(git.getRepository().getDirectory().getParent(), CheckLevel.GIT_REPO_ONLY);
        SanityCheck.check(git.getRepository().getDirectory().getParent(), CheckLevel.NO_UNCOMMITTED_CHANGES);
    }
}
