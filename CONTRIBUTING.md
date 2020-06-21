# Contributing to Gradle Plugin Development Toolbox

:+1::tada: First off, thanks for taking the time to contribute! :tada::+1:

This guide explains how to:

* maximize the chance of your changes being accepted
* work on the code base
* get help if you encounter trouble

## Get in touch

Before starting to work on a feature or a fix, please open an issue to discuss the use case or bug with us.
This can save both you and us a lot of time.
For any non-trivial change, we'll ask you to create a short design document explaining:

* Why is this change done? What's the use case?
* What will the API look like? (For new features)
* What test cases should it have? What could go wrong?
* How will it roughly be implemented? (We'll happily provide code pointers to save you time)

This can be done directly inside the GitHub issue or (for large changes) you can share a Google Doc with us.

## Accept Developer Certificate of Origin

In order for your contributions to be accepted, you must [sign off](https://git-scm.com/docs/git-commit#git-commit---signoff) your Git commits to indicate that you agree to the terms of [Developer Certificate of Origin](https://developercertificate.org/).

## Follow the Code of Conduct

This project and everyone participating in it is governed by the [Code of Conduct](CODE_OF_CONDUCT.adoc). By participating, you are expected to uphold this code. Please report unacceptable behavior to daniel@lacasse.io.

## Making Changes

### Development Setup

In order to make changes to Gradle Plugin Development Toolbox, you'll need:

* A text editor or IDE. We use and recommend [IntelliJ IDEA CE](http://www.jetbrains.com/idea/).
* A [Java Development Kit](http://jdk.java.net/) (JDK) version 8.
* [git](https://git-scm.com/) and a [GitHub account](https://github.com/join).

We uses pull requests for contributions. Fork [gradle-plugins/toolbox](https://github.com/gradle-plugins/toolbox) and clone your fork. Configure your Git username and email with

    git config user.name 'First Last'
    git config user.email user@example.com

### IntelliJ

You require IntelliJ 2018.3.1 or newer.
- Open the `build.gradle.kts` file with IntelliJ and choose "Open as Project"
- Make sure "Create separate module per source set" is selected
- Make sure  "Use default gradle wrapper" is selected
- Select a Java 8 VM as "Gradle JVM"
- In the "File already exists" dialogue, choose "Yes" to overwrite
- In the "Open Project" dialogue, choose "Delete Existing Project and Import"
- Revert the Git changes to files in the `.idea` folder

### Code Change Guidelines

All code contributions should contain the following:

* Unit Tests (using [Spock](http://spockframework.org/spock/docs/1.2/index.html)) for any logic introduced
* Integration Test coverage of the bug/feature at the level of build execution. Please annotate tests guarding against a specific GitHub issue `@Issue("gradle-plugins/toolbox#42")`.

### Development Workflow

After making changes, you can test them in 2 ways:

To run tests, execute `./gradlew :<subproject>:check` where `<subproject>` is the name of the sub-project that has changed.
For example: `./gradlew :gradle-plugin-development:check`.

### Creating Commits And Writing Commit Messages

The commit messages that accompany your code changes are an important piece of documentation, please follow these guidelines when writing commit messages:

* Keep commits discrete: avoid including multiple unrelated changes in a single commit
* Keep commits self-contained: avoid spreading a single change across multiple commits. A single commit should make sense in isolation
* If your commit pertains to a GitHub issue, include (`Issue: #123`) in the commit message on a separate line
* [Sign off](https://git-scm.com/docs/git-commit#git-commit---signoff) your commits to indicate that you agree to the terms of [Developer Certificate of Origin](https://developercertificate.org/).

### Submitting Your Change

After you submit your pull request, a maintainer will review it.
It may take several iterations, so don't get discouraged by change requests.

### Signing Off Commits After Submitting a Pull Request

Pull requests are automatically verified that all commit messages contain the Signed-off-by line with an email address that matches the commit author.
In case you didn't sign off your commits before creating a pull request, you can still fix that to confirm that you agree to the terms of [Developer Certificate of Origin](https://developercertificate.org/).

To sign off a single commit:

`git commit --amend --signoff`

To sign off one or multiple commits:

`git filter-branch --msg-filter "cat - && echo && echo 'Signed-off-by: Your Name <Your.Name@example.com>'" HEAD`

Then force push your branch:

`git push --force origin test-branch`

## Getting Help

If you run into any trouble, please reach out to us on the issue you are working on.

## Our Thanks

We deeply appreciate your effort toward improving Gradle Plugin Development.
For any contribution, large or small, you will be immortalized in the `git log`.