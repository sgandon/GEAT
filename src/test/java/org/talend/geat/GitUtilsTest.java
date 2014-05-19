package org.talend.geat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.Assert;
import org.junit.Test;
import org.talend.geat.exception.NotRemoteException;

import com.google.common.io.Files;

public class GitUtilsTest {

    private Git createTempRepo() throws GitAPIException, IOException {
        File tempDir = Files.createTempDir();
        Git.init().setDirectory(tempDir).call();
        Git git = Git.open(tempDir);
        return git;
    }

    @Test
    public void testListBranches() throws IOException, GitAPIException {
        Git git = createTempRepo();
        createInitialCommit(git, "myFile");

        List<String> expected = new ArrayList<String>();
        expected.add("master");

        List<String> listBranches = GitUtils.listBranches(git.getRepository(), ".*");
        Assert.assertEquals(expected, listBranches);

        git.branchCreate().setName("myBranch").call();
        git.branchCreate().setName("tagada").call();

        listBranches = GitUtils.listBranches(git.getRepository(), "master");
        Assert.assertEquals(expected, listBranches);

        listBranches = GitUtils.listBranches(git.getRepository(), "ma.*");
        Assert.assertEquals(expected, listBranches);

        expected.add("myBranch");
        listBranches = GitUtils.listBranches(git.getRepository(), "m.*");
        Assert.assertEquals(expected, listBranches);
    }

    @Test
    public void testHasRemoteBasic() throws IOException, GitAPIException {
        Git git = createTempRepo();

        Assert.assertFalse(GitUtils.hasRemote("origin", git.getRepository()));
    }

    @Test
    public void testHasRemoteTrue() throws IOException, GitAPIException {
        Git git = createTempRepo();
        Git remote = createTempRepo();

        Assert.assertFalse(GitUtils.hasRemote("origin", git.getRepository()));

        StoredConfig config = git.getRepository().getConfig();
        config.setString("remote", "remote-name", "url", remote.getRepository().getDirectory().getAbsolutePath());
        config.save();

        Assert.assertTrue(GitUtils.hasRemote("remote-name", git.getRepository()));
    }

    @Test
    public void testHasRemoteBranchBasic() throws IOException, GitAPIException {
        Git git = createTempRepo();

        Assert.assertFalse(GitUtils.hasRemoteBranch(git.getRepository(), "theBranch"));
    }

    @Test
    public void testHasRemoteBranchTrue() throws IOException, GitAPIException {
        commonTestRemoteBranch(true);
    }

    @Test
    public void testHasRemoteBranchFalse() throws IOException, GitAPIException {
        commonTestRemoteBranch(false);
    }

    private Git commonTestRemoteBranch(boolean branch) throws GitAPIException, IOException {
        Git git = createTempRepo();
        Git remote = createTempRepo();

        Assert.assertFalse(GitUtils.hasRemote("origin", git.getRepository()));
        createInitialCommit(remote, "aFile");
        if (branch) remote.branchCreate().setName("theBranch").call();

        StoredConfig config = git.getRepository().getConfig();
        config.setString("remote", "origin", "url", remote.getRepository().getDirectory().getParent());
        config.save();

        Assert.assertEquals(branch, GitUtils.hasRemoteBranch(git.getRepository(), "theBranch"));

        return git;
    }

    private void createInitialCommit(Git git, String fileName) throws IOException, GitAPIException {
        File aFile = new File(git.getRepository().getDirectory().getAbsoluteFile().getParentFile(), fileName);
        aFile.createNewFile();
        git.add().addFilepattern(fileName).call();
        git.commit().setMessage("Initial commit (add " + fileName + ")").call();
    }

    @Test
    public void testCallFetchWithLocal() throws GitAPIException, IOException, NotRemoteException {
        Git remote = createTempRepo();
        createInitialCommit(remote, "file1");
        remote.branchCreate().setName("theBranch").call();

        File tempDir = Files.createTempDir();
        Git.cloneRepository().setDirectory(tempDir).setRemote("origin")
                .setURI(remote.getRepository().getDirectory().getParentFile().getAbsolutePath()).call();

        Git git = Git.open(tempDir);
        Assert.assertTrue(GitUtils.hasRemoteBranch(git.getRepository(), "theBranch"));
        GitUtils.callFetch(git.getRepository(), "theBranch");
        Assert.assertTrue(GitUtils.hasLocalBranch(git.getRepository(), "theBranch"));

        remote.checkout().setName("theBranch").call();
        createInitialCommit(remote, "file2");
        File file2 = new File(git.getRepository().getDirectory().getParentFile(), "file2");
        Assert.assertFalse(file2.exists());

        boolean created = GitUtils.callFetch(git.getRepository(), "theBranch");
        Assert.assertFalse(created);
        Assert.assertTrue(GitUtils.hasLocalBranch(git.getRepository(), "theBranch"));
        git.checkout().setName("theBranch").call();
        file2 = new File(git.getRepository().getDirectory().getParentFile(), "file2");
        Assert.assertTrue(file2.exists());
    }

    @Test
    public void testCallFetchNoLocal() throws GitAPIException, IOException, NotRemoteException {
        Git remote = createTempRepo();
        createInitialCommit(remote, "file1");
        remote.branchCreate().setName("theBranch").call();

        File tempDir = Files.createTempDir();
        Git.cloneRepository().setDirectory(tempDir).setRemote("origin")
                .setURI(remote.getRepository().getDirectory().getParentFile().getAbsolutePath()).call();

        Git git = Git.open(tempDir);
        Assert.assertTrue(GitUtils.hasRemoteBranch(git.getRepository(), "theBranch"));

        Assert.assertFalse(GitUtils.hasRemoteBranch(git.getRepository(), "secondBranch"));
        remote.branchCreate().setName("secondBranch").call();
        Assert.assertTrue(GitUtils.hasRemoteBranch(git.getRepository(), "secondBranch"));

        remote.checkout().setName("secondBranch").call();
        createInitialCommit(remote, "file2");
        File file2 = new File(git.getRepository().getDirectory().getParentFile(), "file2");
        Assert.assertFalse(file2.exists());

        boolean created = GitUtils.callFetch(git.getRepository(), "secondBranch");
        Assert.assertTrue(created);
        file2 = new File(git.getRepository().getDirectory().getParentFile(), "file2");
        Assert.assertTrue(file2.exists());
    }

    @Test
    public void testCallFetchWithLocalNoOtherBranches() throws GitAPIException, IOException, NotRemoteException {
        Git remote = createTempRepo();
        createInitialCommit(remote, "file1");
        remote.branchCreate().setName("theBranch").call();
        remote.branchCreate().setName("secondBranch").call();

        File tempDir = Files.createTempDir();
        Git.cloneRepository().setDirectory(tempDir).setRemote("origin")
                .setURI(remote.getRepository().getDirectory().getParentFile().getAbsolutePath()).call();

        Git git = Git.open(tempDir);

        GitUtils.callFetch(git.getRepository(), "theBranch");
        GitUtils.callFetch(git.getRepository(), "secondBranch");

        Assert.assertTrue(GitUtils.hasRemoteBranch(git.getRepository(), "theBranch"));
        Assert.assertTrue(GitUtils.hasLocalBranch(git.getRepository(), "theBranch"));
        Assert.assertTrue(GitUtils.hasRemoteBranch(git.getRepository(), "secondBranch"));
        Assert.assertTrue(GitUtils.hasLocalBranch(git.getRepository(), "secondBranch"));

        remote.checkout().setName("theBranch").call();
        createInitialCommit(remote, "file2");

        remote.checkout().setName("secondBranch").call();
        createInitialCommit(remote, "file3");

        // Fetch only theBranch:
        boolean created = GitUtils.callFetch(git.getRepository(), "theBranch");
        Assert.assertFalse(created);

        // Checkout it to check it's up-to-date
        git.checkout().setName("theBranch").call();
        File file2 = new File(git.getRepository().getDirectory().getParentFile(), "file2");
        Assert.assertTrue(file2.exists());

        // Checkout secondBranch to check it hasn't been fetch
        git.checkout().setName("secondBranch").call();
        File file3 = new File(git.getRepository().getDirectory().getParentFile(), "file3");
        Assert.assertFalse(file3.exists());

        Ref ref = git.getRepository().getRef("remotes/origin/theBranch");
        Ref ref2 = git.getRepository().getRef("theBranch");
        Assert.assertEquals(ref.getObjectId(), ref2.getObjectId());

        ref = git.getRepository().getRef("remotes/origin/secondBranch");
        ref2 = git.getRepository().getRef("secondBranch");

        // TODO ideally this should work
        // Assert.assertEquals(ref.getObjectId(), ref2.getObjectId());
    }

    @Test
    public void testCallFetchNoLocalNoOtherBranches() throws GitAPIException, IOException, NotRemoteException {
        Git remote = createTempRepo();
        createInitialCommit(remote, "file1");
        remote.branchCreate().setName("theBranch").call();

        File tempDir = Files.createTempDir();
        Git.cloneRepository().setDirectory(tempDir).setRemote("origin")
                .setURI(remote.getRepository().getDirectory().getParentFile().getAbsolutePath()).call();

        Git git = Git.open(tempDir);
        Assert.assertTrue(GitUtils.hasRemoteBranch(git.getRepository(), "theBranch"));

        Assert.assertFalse(GitUtils.hasRemoteBranch(git.getRepository(), "secondBranch"));
        remote.branchCreate().setName("secondBranch").call();
        Assert.assertTrue(GitUtils.hasRemoteBranch(git.getRepository(), "secondBranch"));
        remote.checkout().setName("secondBranch").call();
        createInitialCommit(remote, "file2");

        Assert.assertFalse(GitUtils.hasRemoteBranch(git.getRepository(), "thirdBranch"));
        remote.branchCreate().setName("thirdBranch").call();
        Assert.assertTrue(GitUtils.hasRemoteBranch(git.getRepository(), "thirdBranch"));
        remote.checkout().setName("thirdBranch").call();
        createInitialCommit(remote, "file3");

        boolean created = GitUtils.callFetch(git.getRepository(), "secondBranch");
        Assert.assertTrue(created);

        // Test if secondBranch has been fetch, and other branches (like thirdBranch) are not:
        git.checkout().setName("secondBranch").call();
        try {
            git.checkout().setName("thirdBranch").call();
            Assert.fail("Exception should be raised here");
        } catch (RefNotFoundException e) {
        }
    }

    @Test
    public void testCallFetchNoRemote() throws GitAPIException, IOException {
        Git remote = createTempRepo();
        createInitialCommit(remote, "file1");
        remote.branchCreate().setName("theBranch").call();

        File tempDir = Files.createTempDir();
        Git.cloneRepository().setDirectory(tempDir).setRemote("origin")
                .setURI(remote.getRepository().getDirectory().getParentFile().getAbsolutePath()).call();

        Git git = Git.open(tempDir);

        try {
            GitUtils.callFetch(git.getRepository(), "anotherBranch");
            Assert.fail("Exception should be raised here");
        } catch (NotRemoteException e) {
        }
    }
}
