/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.fixtures.vcs;

import dev.gradleplugins.fixtures.file.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.submodule.SubmoduleWalk;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public class GitFileRepository implements GitRepository, AutoCloseable {
    private final String repositoryName;
    private final File parentDirectory;
    private Git git;

    public GitFileRepository(String repositoryName, File parentDirectory) {
        this.repositoryName = repositoryName;
        this.parentDirectory = parentDirectory;
    }

    public GitFileRepository(File parentDirectory) {
        this("repo", parentDirectory);
    }

    /**
     * Returns the underlying Git object from JGit.
     *
     * Please consider adding more convenience methods to this fixture over using "raw" Git APIs.
     */
    public Git getGit() {
        return git;
    }

    public String getName() {
        return repositoryName;
    }

    /**
     * Factory method for creating a GitRepository without using a JUnit @Rule.
     *
     * Creates a repository in the given repoDir.
     */
    public static GitFileRepository init(File repositoryDirectory) throws GitAPIException {
        GitFileRepository repo = new GitFileRepository(repositoryDirectory);
        repo.createGitRepo(repositoryDirectory);
        return repo;
    }

//    @Override
//    protected void before() throws GitAPIException {
//        createGitRepo(new File(parentDirectory, repositoryName));
//    }

    private void createGitRepo(File repositoryDirectory) throws GitAPIException {
        git = Git.init().setDirectory(repositoryDirectory).call();
    }

//    @Override
//    protected void after() {
//        close();
//    }

    /**
     * Clean up any held resources
     * (called automatically when used as a @Rule)
     */
    @Override
    public void close() {
        git.close();
    }

    public RevCommit addSubmodule(GitFileRepository submoduleRepo) throws GitAPIException {
        try (Repository submodule = git.submoduleAdd()
                .setURI(submoduleRepo.getWorkTree().toString())
                .setPath(submoduleRepo.getName())
                .call()) {
            return commit("add submodule " + submoduleRepo.getName(), submoduleRepo.getName());
        }
    }

    /**
     * Updates any submodules in this repository to the latest in the submodule origin repository
     */
    public RevCommit updateSubmodulesToLatest() throws GitAPIException {
        List<String> submodulePaths = new ArrayList<>();
        try {
            try (SubmoduleWalk walker = SubmoduleWalk.forIndex(git.getRepository())) {
                while (walker.next()) {
                    try (Repository submodule = walker.getRepository()) {
                        submodulePaths.add(walker.getPath());
                        Git.wrap(submodule).pull().call();
                    }
                }
            }
            return commit("update submodules", submodulePaths.toArray(new String[0]));
        } catch (IOException e) {
            return rethrow(e);
        }
    }

    /**
     * Commits changes to the given paths.  The paths are passed as-is to the underlying JGit library.
     */
    public RevCommit commit(String message, String... paths) throws GitAPIException {
        AddCommand add = git.add();
        for (String path : paths) {
            add.addFilepattern(path);
        }
        add.call();
        return commit(message);
    }

    /**
     * Commits all changes in the working tree. This is approximately {@code git commit -am "message"}
     */
    @Override
    public RevCommit commit(String message) throws GitAPIException {
        // Commit all changes in the working tree
        AddCommand add = git.add();
        for (File file : FileUtils.listFiles(getWorkTree(), null, true)) {
            add.addFilepattern(relativePath(file));
        }
        add.call();
        final CommitCommand commit = git.commit();
        commit.setSign(false);
        return commit.setMessage(message).call();
    }

    @Override
    public Ref createBranch(String branchName) throws GitAPIException {
        return git.branchCreate().setName(branchName).call();
    }

    @Override
    public Ref checkout(String branchName) throws GitAPIException {
        return git.checkout().setName(branchName).call();
    }

    @Override
    public Ref createLightWeightTag(String tagName) throws GitAPIException {
        return git.tag().setName(tagName).call();
    }

    public Ref createAnnotatedTag(String tagName, String message) throws GitAPIException {
        return git.tag().setName(tagName).setAnnotated(true).setMessage(message).call();
    }

    public Ref getHead() throws IOException {
        return git.getRepository().findRef("HEAD");
    }

    @Override
    public File getWorkTree() {
        return git.getRepository().getWorkTree();
    }

    @Override
    public File file(Object... path) {
        return FileSystemUtils.file(getWorkTree(), path);
    }

    @Override
    public URI getUrl() {
        return getWorkTree().toURI();
    }

    private String relativePath(File file) {
        return getUrl().relativize(file.toURI()).toString();
    }

    public String getId() {
        return "git-repo:" + getUrl().toASCIIString();
    }
}
