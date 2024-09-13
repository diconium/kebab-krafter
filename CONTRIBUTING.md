# Contributing Guide

- Contributing to KEBAB-KRAFTER is fairly easy. This document shows you how to get started.

## General
- The [documentation guide](https://docs) is the ideal beginner to explore the project and to get started.

## Submitting changes

- Fork this repo
  - <https://github.com/diconium/mcc-network-generator/fork>
- Check out a new branch base and name it to what you intend to do:
  - Example:
    ````
    $ git checkout -b BRANCH_NAME
    ````
    If you get an error, you may need to fetch fooBar first by using
    ````
    $ git remote update && git fetch
    ````
  - Use one branch per fix / feature
- Commit your changes
  - Please provide a git message that explains what you've done
  - Please make sure your commits follow the [conventions](https://github.com/diconium/mcc-network-generator/CONTRIBUTING.md#commit-messages)
  - Commit to the forked repository
  - Example:
    ````
    $ git commit -am 'Add some fooBar'
    ````
- Push to the branch
  - Example:
    ````
    $ git push origin BRANCH_NAME
    ````
- Make a pull request
  - Make sure you send the PR to the <code>fooBar</code> branch

