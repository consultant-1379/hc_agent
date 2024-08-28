# Change Log

## 1.0.3+14

 *Automatically updating VERSION_PREFIX to 1.1.0
 *DND-37538: Network policies impacts+fixes
 *DND-37511: Introduction of Common Base OS Micro Image into HCAgent,SC-Manager
 and SC-lmconsumer
 *DND-41773: Upgrade of Micro Common BaseOS Image for HC-Agent, SC-Manager,
 SC-Lmconsumer to latest version 5.14.0
 *DND-32014: Direct log shipping for SC application - Implementation
 *DND-42223: 3pp uplifts in hc-agent and sc-manager repos
 *DND-44901: Duplicate keys in SC Helm Charts
 *Bugfix DND-45104: Adding helm-dr-check for PreCodeReview.
 *DND-44341: Upgrade of Micro Common BaseOS Image for HC-Agent, SC-Manager,
 SC-Lmconsumer to latest version 5.17.0
 *DND-45104: Final FOSS documents for hcagent and scmanager

## 1.0.3+34

 * Fix bug DND-37710: HC Agent repository: images created with false image
 product number label
 * Adapt logshipper sidecar parameter to ADPPRG-126089 and updated logshipper
 configmap for dynamic sidecar
 * Adapt HC-Agent charts to comply with dynamic logshipper sidecar integration
 * Update cbos version
 * DND-38694: Discard queries on deprecated k8s API objects
 (change kubernetes-java-client from 15.0.0 to 17.0.1)
 * DND-37540: Network Policies: Intra-SC Application Pods Interworking-SC Common
 * DND-37862: 3pp uplifts in hc-agent and sc-manager repos
 * DND-38899: Upgrade of Common BaseOS Image for HC-Agent to latest version
 5.10.0
 * BugFix DND-39423: Hadolint: DL3059: HC-Agent - Multiple consecutive RUN instructions
 to be consolidated
 * Add build version and owner name on Jenkins for PreCodeReview, Drop and Pra
 * Fix micrometer version, delete unused FOSS yaml files and added nimbus library
 yaml file
 * Removed logshipper-ca.yaml and log-shipper version from product-info
 * Minor changes in values.yaml parameters regarding sidecar
 * Changed all deprecated parameters for sidecar with the new ones
 * Fix issue with git-tags (use ekoteva credentials for git tags and change git
 repo path to url)

## 1.0.2+2

 * Update Logshipper to latest PRA

## 1.0.1+11

 * DND-34190: Log Severity Level should be possible to change (add log severity
 configmap, monitor it for any changes, apply any valid changes in log severity
 level, include logback in JVM parameters)
 * BugFix_DND-33985: HCAgent fault mapping file needs renaming
 * Remove references to MongoDB from severities configmap and remove MongoDB TC
 * Uplift dependencies
 * DND-34554: FOSS evaluation adaptation - hc-agent
 * Correct logback parameter path in Dockefile
 * Upgrade cbos image

## 1.0.0+3

 * HC Agent microservice moved-out of SC application
 * Maven dependencies forced the upload of apal/slf/rx-kafka/utilities to
 proj-5g-sc-maven-dev-local repo
 * New Jenkins pipelines created for the automatic build of hcagent
 microservice source code, docker
 image and helm chart with the parallel upload on specific repositories
 * New rulesets created that will be used by the Jenkins pipelines or manual
 triggering actions

## 1.0.1-0

 * Latest utilities with LogLevelChanger included
 * Support of dynamic change of Log Severity Level, using logcontrol configmag
 * Adoption of DR-470222-010 (direct streaming) & DR-D1114-040-A,D(debug level)
 * Align container paths
