#!/bin/bash


SCRIPT_BASE_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
IOT_OSE_PROJECT="iot-ose"
MQ_USER="iotuser"
MQ_PASSWORD="iotuser"
KIE_USER="kieuser"
KIE_PASSWORD="kieuser1!"

echo "Setting up OpenShift IoT Demo"

echo
echo "Creating Project: ${IOT_OSE_PROJECT}..."
echo
oc new-project ${IOT_OSE_PROJECT} >/dev/null 2>&1

echo
echo "Creating ImageStreams..."
echo
oc create -n ${IOT_OSE_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/jboss-image-streams.json
oc create -n ${IOT_OSE_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/fis-image-streams.json

echo
echo "Pausing 10 Seconds..."
echo
sleep 10

echo
echo "Importing ImageStreams..."
echo
oc import-image -n ${IOT_OSE_PROJECT} jboss-decisionserver63-openshift --all=true
oc import-image -n ${IOT_OSE_PROJECT} jboss-amq-62 --all=true
oc import-image -n ${IOT_OSE_PROJECT} fis-karaf-openshift --all=true

echo
echo "Deploying AMQ..."
echo
oc process -v=MQ_USERNAME=${MQ_USER},MQ_PASSWORD=${MQ_PASSWORD},IMAGE_STREAM_NAMESPACE=${IOT_OSE_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/amq62-basic.json | oc create -n ${IOT_OSE_PROJECT} -f-

echo
echo "Exposing MQTT Route..."
echo
oc expose svc -n ${IOT_OSE_PROJECT} broker-amq-mqtt

echo
echo "Deploying Decision Server..."
echo
oc process -v=KIE_SERVER_USER="${KIE_USER}",KIE_SERVER_PASSWORD="${KIE_PASSWORD}",IMAGE_STREAM_NAMESPACE=${IOT_OSE_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/decisionserver63-basic-s2i.json | oc create -n ${IOT_OSE_PROJECT} -f-

echo
echo "Deploying FIS Application..."
echo
oc process -v=KIE_APP_USER="${KIE_USER}",KIE_APP_PASSWORD="${KIE_PASSWORD}",BROKER_AMQ_USERNAME="${MQ_USER}",BROKER_AMQ_PASSWORD="${MQ_PASSWORD}",IMAGE_STREAM_NAMESPACE=${IOT_OSE_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/fis-generic-template-build.json | oc create -n ${IOT_OSE_PROJECT} -f-

echo
echo "Deploying Software Sensor Application..."
echo
oc process -v=MQTT_USERNAME="${MQ_USER}",MQTT_PASSWORD="${MQ_PASSWORD}" -f ${SCRIPT_BASE_DIR}/support/templates/software-sensor-template.json | oc create -n ${IOT_OSE_PROJECT} -f-

echo
echo "OpenShift IoT Demo Setup Complete."
echo