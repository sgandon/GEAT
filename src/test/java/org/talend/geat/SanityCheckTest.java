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
        System.setProperty("user.dir", "/one/folder/that/should/not/exists");
        SanityCheck.check(CheckLevel.NONE);
    }

    @Test
    public void testCheckNone2() throws GitAPIException, IncorrectRepositoryStateException {
        File tempDir = Files.createTempDir();
        System.setProperty("user.dir", tempDir.getAbsolutePath());

        SanityCheck.check(CheckLevel.NONE);
    }

    @Test
    public void testCheckNone3() throws GitAPIException, IncorrectRepositoryStateException {
        File tempDir = Files.createTempDir();
        System.setProperty("user.dir", tempDir.getAbsolutePath());

        Git.init().setDirectory(tempDir).call();
        SanityCheck.check(CheckLevel.NONE);
    }

    @Test
    public void testCheckGitRepoOnlyNonExistingFolder() throws IncorrectRepositoryStateException {
        thrown.expect(IncorrectRepositoryStateException.class);
        System.setProperty("user.dir", "/one/folder/that/should/not/exists");

        SanityCheck.check(CheckLevel.GIT_REPO_ONLY);
    }

    @Test
    public void testCheckGitRepoOnlyExistingFolder() throws IncorrectRepositoryStateException {
        thrown.expect(IncorrectRepositoryStateException.class);
        File tempDir = Files.createTempDir();
        System.setProperty("user.dir", tempDir.getAbsolutePath());

        SanityCheck.check(CheckLevel.GIT_REPO_ONLY);
    }

    @Test
    public void testCheckGitRepoOnlyGitRepo() throws GitAPIException, IncorrectRepositoryStateException {
        File tempDir = Files.createTempDir();
        System.setProperty("user.dir", tempDir.getAbsolutePath());
        Git.init().setDirectory(tempDir).call();
        SanityCheck.check(CheckLevel.GIT_REPO_ONLY);
    }

    @Test
    public void testCheckUntracked1() throws GitAPIException, IOException, IncorrectRepositoryStateException {
        thrown.expect(IncorrectRepositoryStateException.class);
        File tempDir = Files.createTempDir();
        System.setProperty("user.dir", tempDir.getAbsolutePath());

        Git.init().setDirectory(tempDir).call();

        Git repo = Git.open(tempDir);
        File child = new File(tempDir, "child");
        child.createNewFile();
        repo.add().addFilepattern("child").call();

        SanityCheck.check(CheckLevel.NO_UNCOMMITTED_CHANGES);
    }

    @Test
    public void testCheckUntracked2() throws GitAPIException, IOException, IncorrectRepositoryStateException {
        File tempDir = Files.createTempDir();
        System.setProperty("user.dir", tempDir.getAbsolutePath());

        Git.init().setDirectory(tempDir).call();

        Git repo = Git.open(tempDir);
        File child = new File(tempDir, "child");
        child.createNewFile();
        repo.add().addFilepattern("child").call();

        SanityCheck.check(CheckLevel.GIT_REPO_ONLY);
    }

    @Test
    public void testCheckUntracked3() throws GitAPIException, IOException, IncorrectRepositoryStateException {
        File tempDir = Files.createTempDir();
        System.setProperty("user.dir", tempDir.getAbsolutePath());

        Git.init().setDirectory(tempDir).call();

        Git repo = Git.open(tempDir);
        File child = new File(tempDir, "child");
        child.createNewFile();
        repo.add().addFilepattern("child").call();

        repo.commit().setMessage("Initial commit").call();
        SanityCheck.check(CheckLevel.NO_UNCOMMITTED_CHANGES);
    }

    @Test
    public void testCheckUntrackedWithUtils() throws GitAPIException, IOException, IncorrectRepositoryStateException {
        Git git = JUnitUtils.createTempRepo();
        System.setProperty("user.dir", git.getRepository().getDirectory().getParent());
        JUnitUtils.createInitialCommit(git, "file1");
        SanityCheck.check(CheckLevel.GIT_REPO_ONLY);
        SanityCheck.check(CheckLevel.NO_UNCOMMITTED_CHANGES);
    }
}
