# Git Easy At Talend
Tool make usage of git workflow easier.

Workflow is defined [here](https://wiki.talend.com/pages/viewpage.action?pageId=7800031)

![](https://travis-ci.org/smallet/GEAT.svg?branch=master)

## Basics
#### Work on a new feature?
    geat feature-start TDI-28776-memoryLeak
create a local branch feature/TDI-28776-memoryLeak based on master

#### Finish your feature?
    geat feature-finish TDI-28776-memoryLeak
**squash** local branch feature/TDI-28776-memoryLeak on master and delete local branch

#### Want to share your feature?
    geat feature-push TDI-28776-memoryLeak
create a new remote branch and set local branch to track it

## Advanced
#### Rebase feature instead of squash
    geat feature-finish TDI-28776-memoryLeak rebase
**rebase** local branch feature/TDI-28776-memoryLeak on master and delete local branch

##### or
     git config geat.finishmergemode rebase
to change the geat-finish default behavior
