IoT OpenShift Demo
===============

Demonstration of Internet of Things (IoT) methodologies and technologies running on the Red Hat OpenShift Container Platform

*Please note: This repository is currently under development and the feature set/components are subject to change without notice*

## Components

* [AMQ and MQTT](https://access.redhat.com/documentation/en/red-hat-xpaas/0/paged/red-hat-xpaas-a-mq-image/)
* [Realtime Decision Server](https://access.redhat.com/documentation/en/red-hat-xpaas/0/paged/red-hat-xpaas-a-mq-image/)
* [Fuse Integration Services](https://access.redhat.com/documentation/en/red-hat-xpaas/version-0/red-hat-xpaas-fuse-integration-services-image/)
* [Apache Zeppelin](https://zeppelin.apache.org/)
* Simulated Software Sensor

## Prerequisites

The following prerequisites must be satisfied prior to running the demo

* Git client
* Access to an OpenShift environment
* OpenShift Command Line Interface (CLI)

## Setup

An [init.sh](init.sh) script is available to automate the provisioning process. Since the demonstration runs in the OpenShift Container Platform, an environment must be accessible. Execute this script on a machine with the OpenShift Command Line (CLI) tools already installed and authenticated to the platform

Execute the script to setup the demo

```
./init.sh
```

## Post Script Configuration

The majority of the components are automatically deployed and configured. The Zeppelin visualization tool requires several manual step to fully configure the tool for records that are produced by the applications.

First, login to the OpenShift environment and locate the **iot-ose** project. Select the project and locate the *zeppelin* service on the overview page. Next to the service is the exposed HTTP url. Select the URL to launch the Zeppelin web console. 

The first step is to configure zeppelin to communicate with the backend database. The integration between the tool and backend services is accomplished through *interpreters*. On the top right corner, locate and hover over the *anonymous* username and select **Interpreter**.

Create a new interpreter called *iot-ose* by selecting **Create** on the top right hand corner of the page. Enter **iot-ose** in the name checkbox and select **psql** in the Interpreter dropdown. By selecting *psql*, several name/value pairs are automatically pre populated. Enter the following pairs to modify the default configurations.

| Name  | Value |
|----------|---------|
| postgresql.password	| postgresiot |
| postgresql.url | jdbc:postgresql://postgresql:5432/iot |
| postgresql.user | postgresiot |

Click **Save** to apply the changes

The visualizations are contained in *Notebooks*. A preconfigured base note for the project are available in the *support/zeppelin* folder in a file called [iot-ose.json](support/zeppelin/iot-ose.json).

This notebook can be imported to Zeppelin by selecting the Zeppelin logo on the top left to return to the homepage. Under notebook, select **Import note**. Select **Choose A JSON Here** and navigate to the json file.

Select the **iot-ose** note that is now available under the notebook section. 

Finally, enable the *iot-ose* interpreter created earlier by selecting the gear on the top right corner of the page representing the **interpreter Binding**. Locate the *iot-ose* interpreter and ensure that it is enabled by clicking on the interpreter so that is highlighted in blue. Drag the interpreter the top of the list to ensure that it is placed with a higher precedence than another interpreters. Click **Save** to apply the changes

Execute all visualizations by hitting the play button on the top lefthand corner of the page next to the name of the note. 