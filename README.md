IoT OpenShift Demo
===============

This project demonstrates the use of Internet of Things (IoT) and OpenShift

## Prerequisites

The following prerequisites must be satisfied prior to running the demo

* Git client
* Access to an OpenShift environment
* OpenShift Command Line Interface (CLI)

## Setup

An [init.sh](init.sh) script is available to quickly configure an OpenShift environment for the demo

Execute the script to setup the demo

```
./init.sh
```

## Validation

The execution of the script in the previous section will trigger asynchronous builds and deployments of AMQ, Decision Server, and Fuse Integration Service components. Validate the following sections:

* Validate KIE and FIS builds completed successfully

```
oc get builds
```

* Validate AMQ, KIE and FIS pods are running

```
oc get pods | grep Running
```

## Send a Test Message

The *iot-ose-software-sensor* project is configured with a testing application to send messages to AMQ via the *mqtt* protocol. 

To avoid communicating with the OpenShift routing layer, the OpenShift CLI can be used to communicate directly with the pod network by forwarding a local port to a container port.

First, locate the name of the AMQ pod

```
oc get pods | grep amq.*Running | awk '{ print $1 }'
```

Forward the mqtt port using the name of the pod retrieved in the previous command

```
oc port-forward -p <AMQ_POD> 1883:1883
```

Build the *iot-ose-software-sensor* project using maven by navigating to the *iot-ose-software-sensor* folder and executing a build

```
mvn clean install
```

The build produces an executable jar with the necessary dependencies to send a test message. Execute the following command to execute the jar and send a message

```
java -jar target/iot-ose-software-sensor-jar-with-dependencies.jar
```