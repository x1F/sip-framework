# Contributing to SIP

[TOC]

First off, thanks for your contribution! :+1:

SIP is released under the Apache 2.0 license. If you would like to contribute you are very welcome to do so and this document should help you having a good start.

Please note we have a code of conduct, please follow it in all your interactions with the project.

## Code of conduct

This project adheres to the Contributor Covenant [code of conduct](./CODE_OF_CONDUCT.md).

By participating, you are expected to uphold this code. Please report unacceptable behavior to the given email address.

## How can I contribute

### Reporting bugs

We use GitHub issues to track bugs. Bug reports should be done by using and completing the bug tracking template.

Please handle possibly security vulnerabilities with special care as described below.

### Reporting security vulnerabilities

If you think you have found a security vulnerability please *DO NOT* disclose it publicly until we've had a chance to fix it as this may result in an unnecessary security threat. We will handle these requests with special care.

Please contact us with templated bug report via email at: sip@x1f.one

### Requesting features

Feature requests can be done by creating a Github Issue using the feature request template.

### Pull requests (to get YOUR contribution merged)

Another option to contribute is to fix any bugs from the bug list, implement features, extend functionality, adapt the documentation or whatever else you can imagine! :+1:

To get your adaptions merged you need to create a pull request. You have to complete the provided pull request template. We will take care of the submitted pull requests and to mention this once again: Thank you very much for your contribution!

## Guidelines

We welcome everyone's work and engagement to evolve SIP. To make our life easier we defined a couple of guidelines to follow while working on the code, and we ask everyone to adhere to these points. During the pull request process the reviewer will check if the pull request adheres to the following rules. The rules are questions you should answer before creating a pull request:

### Coding

- Have you avoided to use field based autowiring? (see [here](http://odrotbohm.de/2013/11/why-field-injection-is-evil/) why we chose to use constructor autowiring)
- Are all your unit tests written with junit5 and assertions with assertj ([see](https://assertj.github.io/doc/))?
- Does your code not add any new compile warnings?
- Does your contribution contain necessary and useful javadoc and is the linting process successful?
- Is the maven project version a SNAPSHOT version?
- Are all dependencies you added RELEASE versions?
- Did you change something in the documentation? Then run `./run-mkdocs-server.sh` and check that your changes look good (mkdocs server will be running on port 8000)

### Pull request process

- Does your contribution follow the Google Java Coding style? (you can find it [here](https://github.com/google/google-java-format))
- Does your pull request only contain changes related to the topic the branch was intended for?
- Does your pull request contain useful and necessary tests?
- Was the Sonarqube analysis of your PR successful and all quality gates are met?
- Have you added a complete, valid and self explaining changelog?
    - Changelogs have to be put into one of these folders: `changelogs/bugfix`, `changelogs/documentation`, `changelogs/feature`, `changelogs/major`
    - The name of the changelog file has to end with the suffix `.json`
    - The format of a changelog file is as follow (don't copy the comments, only the real json key-value pairs):
      ```json
      {
        # put your github username here
        "author": "stieglma",

        # put the id of the pull request here (yes that means you will need to create the pull
        # request before you can finish writing the changelog entry)
        "pullrequestId": 1,

        # put a precise and short description of your changes here
        "message": "Update documentation, add information about changelog entries for contributors",

        # issue id is optional, there may be pull requests without related issues,
        # so this field can be left out if necessary
        "issue": "123"
      }
      ```
- Is your pull request mergeable?
- Was your branch deleted after merging the sources?
- Does the pull request also contain necessary documentation changes or adaptions
- Have you used and completed the pull request form provided?
