#!/bin/bash

SCRIPT_BASE_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
IOT_OCP_RESOURCES_PROJECT="iot-ocp-resources"
BUILD_CHECK_INTERVAL=5
BUILD_CHECK_TIMES=60
LOG_FILE_NAME="${SCRIPT_BASE_DIR}/infra-setup-$(date +%Y-%m-%d_%H%M%S).log"

function wait_for_running_build() {
    BUILD_NAME="$1-1"
    COUNTER=0
    
    # Check to see if build exists
    while [ ${COUNTER} -lt $BUILD_CHECK_TIMES ]
    do
        
        BUILD_COUNT=$(oc get -n ${IOT_OCP_RESOURCES_PROJECT} builds ${BUILD_NAME} --no-headers | wc -l | awk '{ print $1 }')
        
        if [ $BUILD_COUNT == "1" ]; then
          break  
        fi
        
        if [ $COUNTER -lt $BUILD_CHECK_TIMES ]; then
            COUNTER=$(( $COUNTER + 1 ))
        fi
        
        
        if [ $COUNTER -eq $BUILD_CHECK_TIMES ]; then
          echo "Max Validation Attempts Exceeded. Failed Verifying Application Build..." 2>&1 | tee -a "${LOG_FILE_NAME}"
          exit 1
        fi
                
        sleep $BUILD_CHECK_INTERVAL

    done
    
    COUNTER=0
    
    # Check to see we have a running build
    while [ ${COUNTER} -lt $BUILD_CHECK_TIMES ]
    do
        
        BUILD_STATUS=$(oc get -n ${IOT_OCP_RESOURCES_PROJECT} builds ${APP_NAME}-1 --template='{{ .status.phase }}')
        
        if [ $BUILD_STATUS == "Running" ]; then
          break
        elif [ $BUILD_STATUS == "Failed" ]; then
            echo "Build has failed"
            exit 2
        fi
        
        if [ $COUNTER -lt $BUILD_CHECK_TIMES ]; then
            COUNTER=$(( $COUNTER + 1 ))
        fi
        
        
        if [ $COUNTER -eq $BUILD_CHECK_TIMES ]; then
          echo "Max Validation Attempts Exceeded. Failed Verifying Application Build..." 2>&1 | tee -a "${LOG_FILE_NAME}"
          exit 1
        fi
                
        sleep $BUILD_CHECK_INTERVAL

    done   
    
}

function validate_build() {
    APP_NAME=$1
    
    # Wait for a build to be running
    echo
    echo "Waiting for a running build.."
    echo
    wait_for_running_build "${APP_NAME}"

    oc logs -n ${IOT_OCP_RESOURCES_PROJECT} build/${APP_NAME}-1 -f 2>&1 | tee -a "${LOG_FILE_NAME}"

    sleep 10

    BUILD_STATUS=$(oc get -n ${IOT_OCP_RESOURCES_PROJECT} build ${APP_NAME}-1 --template='{{ .status.phase }}')

    if [ ${BUILD_STATUS} != "Complete" ]; then
        echo
        echo "${APP_NAME} build did not complete successfully. Status is: ${BUILD_STATUS}" 2>&1 | tee -a "${LOG_FILE_NAME}"
        echo
        exit 1
    fi

    # Pause 10 seconds
    sleep 10

}



echo 2>&1 | tee -a "${LOG_FILE_NAME}"
echo "== Setting Up Red Hat Summit 2017 Lab Infrastructure Environment ==" 2>&1 | tee -a "${LOG_FILE_NAME}"
echo 2>&1 | tee -a "${LOG_FILE_NAME}"

echo 2>&1 | tee -a "${LOG_FILE_NAME}"
echo "Starting MiniShift Environment..." 2>&1 | tee -a "${LOG_FILE_NAME}"
echo 2>&1 | tee -a "${LOG_FILE_NAME}"

minishift start

echo 2>&1 | tee -a "${LOG_FILE_NAME}"
echo "Prepping Minishift Machine..." 2>&1 | tee -a "${LOG_FILE_NAME}"
echo 2>&1 | tee -a "${LOG_FILE_NAME}"

echo "
  sudo mkdir -p /var/lib/minishift/pv/pv0{1..3}
  sudo chmod -R 777 /var/lib/minishift/pv/pv*
  sudo chmod -R a+w /var/lib/minishift/pv/pv*
  sudo chcon -R -t svirt_sandbox_file_t /var/lib/minishift/pv/*
  sudo restorecon -R /var/lib/minishift/pv/
  " | minishift ssh

echo 2>&1 | tee -a "${LOG_FILE_NAME}"
echo "Logging Into Environment..." 2>&1 | tee -a "${LOG_FILE_NAME}"
echo 2>&1 | tee -a "${LOG_FILE_NAME}"

oc login -u "system:admin" --insecure-skip-tls-verify=true 2>&1 | tee -a ${LOG_FILE_NAME}

echo 2>&1 | tee -a "${LOG_FILE_NAME}"
echo "Creating Resources Project..." 2>&1 | tee -a "${LOG_FILE_NAME}"
echo 2>&1 | tee -a "${LOG_FILE_NAME}"

oc new-project ${IOT_OCP_RESOURCES_PROJECT} --description="Contains resources to support Summit 2017 IoT Lab" --display-name="Internet of Things (IoT) Resources Project" 2>&1 | tee -a ${LOG_FILE_NAME}


echo 2>&1 | tee -a "${LOG_FILE_NAME}"
echo "Adding ImageStreams ..." 2>&1 | tee -a "${LOG_FILE_NAME}"
echo 2>&1 | tee -a "${LOG_FILE_NAME}"

oc create -n ${IOT_OCP_RESOURCES_PROJECT} -f ${SCRIPT_BASE_DIR}/../../support/templates/jboss-image-streams.json 2>&1 | tee -a "${LOG_FILE_NAME}"
oc create -n ${IOT_OCP_RESOURCES_PROJECT} -f ${SCRIPT_BASE_DIR}/../../support/templates/fis-image-streams.json 2>&1 | tee -a "${LOG_FILE_NAME}"
oc create -n ${IOT_OCP_RESOURCES_PROJECT} -f ${SCRIPT_BASE_DIR}/../../support/templates/openjdk18-openshift-is.json 2>&1 | tee -a "${LOG_FILE_NAME}"
oc create -n ${IOT_OCP_RESOURCES_PROJECT} -f ${SCRIPT_BASE_DIR}/../../support/templates/centos-is.json 2>&1 | tee -a "${LOG_FILE_NAME}"

oc import-image -n ${IOT_OCP_RESOURCES_PROJECT} jboss-decisionserver63-openshift --all=true 2>&1 | tee -a "${LOG_FILE_NAME}"
oc import-image -n ${IOT_OCP_RESOURCES_PROJECT} jboss-amq-62 --all=true 2>&1 | tee -a "${LOG_FILE_NAME}"
oc import-image -n ${IOT_OCP_RESOURCES_PROJECT} fis-karaf-openshift --all=true 2>&1 | tee -a "${LOG_FILE_NAME}"
oc import-image -n ${IOT_OCP_RESOURCES_PROJECT} openjdk18-openshift --all=true 2>&1 | tee -a "${LOG_FILE_NAME}"
oc import-image -n ${IOT_OCP_RESOURCES_PROJECT} centos --all=true 2>&1 | tee -a "${LOG_FILE_NAME}"

echo 2>&1 | tee -a "${LOG_FILE_NAME}"
echo "Adding Persistent Volumes ..." 2>&1 | tee -a "${LOG_FILE_NAME}"
echo 2>&1 | tee -a "${LOG_FILE_NAME}"

oc create -f ${SCRIPT_BASE_DIR}/../image-build/pv/pv01.yaml 2>&1 | tee -a "${LOG_FILE_NAME}"
oc create -f ${SCRIPT_BASE_DIR}/../image-build/pv/pv02.yaml 2>&1 | tee -a "${LOG_FILE_NAME}"
oc create -f ${SCRIPT_BASE_DIR}/../image-build/pv/pv03.yaml 2>&1 | tee -a "${LOG_FILE_NAME}"

echo 2>&1 | tee -a "${LOG_FILE_NAME}"
echo "Processing Shared Resource Template ..." 2>&1 | tee -a "${LOG_FILE_NAME}"
echo 2>&1 | tee -a "${LOG_FILE_NAME}"

oc process -v=NAMESPACE=${IOT_OCP_RESOURCES_PROJECT} -f ${SCRIPT_BASE_DIR}/../shared-resource-template.json | oc create -n ${IOT_OCP_RESOURCES_PROJECT} -f- | tee -a "${LOG_FILE_NAME}"

echo 2>&1 | tee -a "${LOG_FILE_NAME}"
echo "Building Images ..." 2>&1 | tee -a "${LOG_FILE_NAME}"
echo 2>&1 | tee -a "${LOG_FILE_NAME}"

oc process -v=IMAGE_STREAM_NAMESPACE=${IOT_OCP_RESOURCES_PROJECT} -f ${SCRIPT_BASE_DIR}/../image-build/rules-brms-build.json | oc create -n ${IOT_OCP_RESOURCES_PROJECT} -f- | tee -a "${LOG_FILE_NAME}"
validate_build "rules-brms"

oc process -v=IMAGE_STREAM_NAMESPACE=${IOT_OCP_RESOURCES_PROJECT} -f ${SCRIPT_BASE_DIR}/../image-build/integration-fis-build.json | oc create -n ${IOT_OCP_RESOURCES_PROJECT} -f- | tee -a "${LOG_FILE_NAME}"
validate_build "integration-fis"

oc process -v=IMAGE_STREAM_NAMESPACE=${IOT_OCP_RESOURCES_PROJECT} -f ${SCRIPT_BASE_DIR}/../image-build/software-sensor-build.json | oc create -n ${IOT_OCP_RESOURCES_PROJECT} -f- | tee -a "${LOG_FILE_NAME}"
validate_build "software-sensor"

oc process -f ${SCRIPT_BASE_DIR}/../image-build/visualization-zeppelin-build.json | oc create -n ${IOT_OCP_RESOURCES_PROJECT} -f- | tee -a "${LOG_FILE_NAME}"
validate_build "visualization-zeppelin"

echo 2>&1 | tee -a "${LOG_FILE_NAME}"
echo "Pulling Additional Docker Images ..." 2>&1 | tee -a "${LOG_FILE_NAME}"
echo 2>&1 | tee -a "${LOG_FILE_NAME}"

echo "
  sudo docker pull registry.access.redhat.com/rhscl/postgresql-95-rhel7:latest
  sudo docker pull registry.access.redhat.com/jboss-amq-6/amq62-openshift:1.3
  " | minishift ssh