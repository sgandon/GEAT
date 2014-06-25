masteraaadddd
# Git Easy At Talend
Too make usage of git workflow easier.

Workflow is defined [here](https://wiki.talend.com/pages/viewpage.action?pageId=7800031)

<a href="https://travis-ci.org/smallet/GEAT/builds">
![](https://travis-ci.org/smallet/GEAT.svg?branch=master)
</a>


### Work on a new feature?
    geat feature-start TDI-28776-memoryLeak
Create a local branch feature/TDI-28776-memoryLeak based on master

### Finish your feature?
    geat feature-finish TDI-28776-memoryLeak
**squash** local branch feature/TDI-28776-memoryLeak on master and delete local (and remote if exist) branch

### Want to share your feature?
    geat feature-push TDI-28776-memoryLeak
Create a new remote branch and set local branch to track it

### Work on a new bugfix?
    geat bugfix-start TDI-28776-memoryLeak
Create a local branch bugfix/5.4/TDI-28776-memoryLeak based on maintenance/5.4

Startpoint (branch to create this branch on) will be asked if no specified

### Finish your bugfix?
    geat bugfix-finish TDI-28776-memoryLeak
**squash** local branch bugfix/5.4/TDI-28776-memoryLeak on maintenance/5.4 and delete local (and remote if exist) branch

## Advanced
### Rebase feature instead of squash
    geat feature-finish TDI-28776-memoryLeak rebase
**rebase** local branch feature/TDI-28776-memoryLeak on master and delete local branch

#### or
     git config geat.finishmergemode rebase
To change the geat-finish default behavior

### Specify bugfix startpoint
to prevent prompt, you can specify startpoint at call:

    geat bugfix-start TDI-28776-memoryLeak 5.4

### Few version of a bugfix
If you have few bugfix branches for the same issue (like bugfix/5.3/TDI-28776-memoryLeak and bugfix/5.4/TDI-28776-memoryLeak for example), you need to specify the target when finish:

    geat bugfix-finish TDI-28776-memoryLeak 5.4
    
If there is only one bugfix branch with the name (local or remote), GEAT will find it.
