# Contributing to SC HCAgent microservice

[TOC]

## Code of Conduct

This project and everyone participating in it is governed by the
[SC Challengers Code of Conduct](CODE_OF_CONDUCT.md}.
By participating, you are expected to uphold this code.

## Project Guardians

The guardians are the maintainer of this project.
They are responsible to moderate the discussion forum,
guide the contributors and review the submitted patches.

 * SC Challengers <IXG-ChallengersTeam@ericsson.onmicrosoft.com>

## Development Environment prerequisites

The development framework for SC HCAgent is based on bob.
To be able to run bob, the following tools need to exist on the host:

 * python3
 * bash
 * docker
 * maven
 * openjdk
 * git

Bob expects you to have a valid docker login towards your docker registry on
the host, currently it can't handle automatic login by itself.
If you are using armdocker, then you can login with the following command:

```shell
docker login armdocker.rnd.ericsson.se
```

## How Can I Contribute?

This guide describes how to contribute change and test it but
any changes should be agreed with project guardians.

## Update base version file

If implementing major or minor features update the file: [VERSION_PREFIX](VERSION_PREFIX).
Always update the file after the Release Pipeline is executed
in order to follow semantic versioning.

### Prerequisite

Make sure you have installed all the [prerequisites of Bob 2.0](
https://gerrit.ericsson.se/plugins/gitiles/adp-cicd/bob/+/HEAD/USER_GUIDE_2.0.md#Prerequisites
).

### Artifactory API Token

For the Push release stage, to be able to use the API token (necessary
to access Artifactory), a Secret Text type Credential needs to be created for the
pipeline's domain (or the Global domain).

The ID of the credential must be repository-tokens-file, and the secret text
should contain a valid API key.

### Build and test

Note: to run the system-test rule the `KUBECONFIG` environment variable
must be set that points to a kubernetes admin.conf
Furthermore for testing the PRI generation the following credentials as environment
variables have to be set properly:

 * `HELM_USERNAME`, `HELM_TOKEN`
 * `GERRIT_USERNAME`, `GERRIT_PASSWORD`

For the `publish` rule for uploading artifacts into artifactory, a valid ARM (Artifactory)
API key is needed, which should be provided in the `ARTIFACTORY_TOKEN`
environment variable.

Run command with default bob rules to build and test your change:

```sh
./bob/bob
```

The command will:

 * lint readme and source code files
 * generate documents
 * build the docker image of the microservice
 * test the helm chart for compliancy towards helm chart design rules
 * create the hem chart archive
 * test the helm chart archive during installation and upgrade

## Commit the change

Make sure that the gerrit `Change-Id:` is inserted by the commit-msg hook.
Always include JIRA issue number in the title.
The following information must be present in a commit message for

```example
Example 1 with Requirement:

Requirement heading

This requirement does awesome things
- Does pizza

Requirement: DND-69699

Change-Id: Ia1147f79572a8cf6c6014528f76ec4932f82cbc1

Example 2 with Troublereport:

Trouble fixing

Troublereport: DND-66999

Change-Id: Ia1147f79572a8cf6c6014528f76ec4932f82cbc2
```

See Architecture Design Rule: [DR-D1101-343-A](
https://confluence.lmera.ericsson.se/display/AA/Artifact+handling+design+rules)

All commit messages that do not match this format will be automatically
discarded by PRI generation tool.

## Push to gerrit

Push change to gerrit and follow gerrit link:

```sh
git push origin HEAD:refs/for/master
```

When pushing to gerrit CI job is executed:

 * [hcagent-precodereview-pipeline](https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/HC-AGENT/job/hcagent-precodereview-pipeline/)

After the verification with two +2, the change could be merged to master.
It is advised to consult [Team Challengers](README.md#Contact) prior any change.

## Review

Send review request to [Team Challengers](README.md#Contact) via gerrit

### Submit

When Submitting the change CI job is executed:

 * [hcagent-drop-pipeline](https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/HC-AGENT/job/hcagent-drop-pipeline/)

Job will run steps to create a new version.

The docker tag will be created and pushed:

 * VERSION_PREFIX-BUILD_NUMBER (Example: 2.0.3-0)

### PRA

The PRA job has to be triggered manually:

 * [hcagent-pra-pipeline](https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/HC-AGENT/job/hcagent-pra-pipeline/)
