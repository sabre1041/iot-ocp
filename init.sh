#!/bin/bash

SCRIPT_BASE_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
IOT_OCP_PROJECT="iot-ocp"
MQ_USER="iotuser"
MQ_PASSWORD="iotuser"
KIE_USER="kieuser"
KIE_PASSWORD="kieuser1!"
GIT_BRANCH="master"
POSTGRESQL_APP_NAME="postgresql"
POSTGRESQL_USERNAME="postgresiot"
POSTGRESQL_PASSWORD="postgresiot"
POSTGRESQL_DATABASE="iot"
BUILD_CHECK_INTERVAL=5
BUILD_CHECK_TIMES=60
DEPLOYMENT_CHECK_INTERVAL=10
DEPLOYMENT_CHECK_TIMES=60

function validate_build_success() {

    FIRST_BUILD_NAME="${1}-1"

    # Get status of finished build
    BUILD_STATUS=$(oc get -n ${IOT_OCP_PROJECT} build ${FIRST_BUILD_NAME} --template='{{ .status.phase }}')

    if [ ${BUILD_STATUS} != "Complete" ]; then
        echo
        echo "Build did not complete successfully. Status is: ${BUILD_STATUS}"
        echo
        exit 1
    fi

}

function wait_for_running_build() {
    BUILD_NAME="$1-1"
    COUNTER=0
    
    # Check to see if build exists
    while [ ${COUNTER} -lt $BUILD_CHECK_TIMES ]
    do
        
        BUILD_COUNT=$(oc get -n ${IOT_OCP_PROJECT} builds ${BUILD_NAME} --no-headers | wc -l | awk '{ print $1 }')
        
        if [ $BUILD_COUNT == "1" ]; then
          break  
        fi
        
        if [ $COUNTER -lt $BUILD_CHECK_TIMES ]; then
            COUNTER=$(( $COUNTER + 1 ))
        fi
        
        
        if [ $COUNTER -eq $BUILD_CHECK_TIMES ]; then
          echo "Max Validation Attempts Exceeded. Failed Verifying Application Build..."
          exit 1
        fi
                
        sleep $BUILD_CHECK_INTERVAL

    done
    
    COUNTER=0
    
    # Check to see we have a running build
    while [ ${COUNTER} -lt $BUILD_CHECK_TIMES ]
    do
        
        BUILD_STATUS=$(oc get -n ${IOT_OCP_PROJECT} builds ${APP_NAME}-1 --template='{{ .status.phase }}')
        
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
          echo "Max Validation Attempts Exceeded. Failed Verifying Application Build..."
          exit 1
        fi
                
        sleep $BUILD_CHECK_INTERVAL

    done   
    
}


function wait_for_application_deployment() {
    
    DC_NAME=$1
    DEPLOYMENT_VERSION=
    RC_NAME=
    COUNTER=0
    
    # Validate Deployment is Active
    while [ ${COUNTER} -lt $DEPLOYMENT_CHECK_TIMES ]
    do
        
        DEPLOYMENT_VERSION=$(oc get -n ${IOT_OCP_PROJECT} dc ${DC_NAME} --template='{{ .status.latestVersion }}')
        
        RC_NAME="${DC_NAME}-${DEPLOYMENT_VERSION}"
        
        if [ ${DEPLOYMENT_VERSION} == "1" ]; then
          break
        fi
        
        if [ $COUNTER -lt $DEPLOYMENT_CHECK_TIMES ]; then
            COUNTER=$(( $COUNTER + 1 ))
        fi
        
        
        if [ $COUNTER -eq $DEPLOYMENT_CHECK_TIMES ]; then
          echo "Max Validation Attempts Exceeded. Failed Verifying Application Deployment..."
          exit 1
        fi        
        sleep $DEPLOYMENT_CHECK_INTERVAL
        
     done
     
     COUNTER=0

     # Validate Deployment Complete
     while [ ${COUNTER} -lt $DEPLOYMENT_CHECK_TIMES ]
     do
        
         DEPLOYMENT_STATUS=$(oc get -n ${IOT_OCP_PROJECT} rc/${RC_NAME} --template '{{ index .metadata.annotations "openshift.io/deployment.phase" }}')
                
         if [ ${DEPLOYMENT_STATUS} == "Complete" ]; then
           break
         elif [ ${DEPLOYMENT_STATUS} == "Failed" ]; then
             echo "Deployment Failed!"
             exit 1
         fi
         
         if [ $COUNTER -lt $DEPLOYMENT_CHECK_TIMES ]; then
             COUNTER=$(( $COUNTER + 1 ))
         fi
         
         
         if [ $COUNTER -eq $DEPLOYMENT_CHECK_TIMES ]; then
           echo "Max Validation Attempts Exceeded. Failed Verifying Application Deployment..."
           exit 1
         fi
                 
         sleep $DEPLOYMENT_CHECK_INTERVAL
        
      done     
     
}

function validate_build_deploy() {
    APP_NAME=$1
    
    # Wait for a build to be running
    echo
    echo "Waiting for a running build.."
    echo
    wait_for_running_build "${APP_NAME}"

    oc logs -n ${IOT_OCP_PROJECT} build/${APP_NAME}-1 -f

    sleep 10

    BUILD_STATUS=$(oc get -n ${IOT_OCP_PROJECT} build ${APP_NAME}-1 --template='{{ .status.phase }}')

    if [ ${BUILD_STATUS} != "Complete" ]; then
        echo
        echo "${APP_NAME} build did not complete successfully. Status is: ${BUILD_STATUS}"
        echo
        exit 1
    fi

    # Pause 10 seconds
    sleep 10
    
    echo
    echo "Waiting for ${APP_NAME} to deploy..."
    echo
    wait_for_application_deployment "${APP_NAME}"

}




echo "Setting up OpenShift IoT Example Project"

echo
echo "Creating Project: ${IOT_OCP_PROJECT}..."
echo
oc new-project ${IOT_OCP_PROJECT} --description="Showcases an Intelligent Internet-of-Things (IoT) Gateway on Red Hat’s OpenShift Container Platform" --display-name="Internet of Things (IoT) OpenShift Demo Project" >/dev/null 2>&1

echo
echo "Creating ImageStreams..."
echo
oc create -n ${IOT_OCP_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/jboss-image-streams.json
oc create -n ${IOT_OCP_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/fis-image-streams.json
oc create -n ${IOT_OCP_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/rhel-is.json

echo
echo "Pausing 10 Seconds..."
echo
sleep 10

echo
echo "Importing ImageStreams..."
echo
oc import-image -n ${IOT_OCP_PROJECT} jboss-decisionserver63-openshift --all=true
oc import-image -n ${IOT_OCP_PROJECT} jboss-amq-62 --all=true
oc import-image -n ${IOT_OCP_PROJECT} fis-karaf-openshift --all=true
oc import-image -n ${IOT_OCP_PROJECT} rhel7 --all=true

echo
echo "Deploying PostgreSQL..."
echo
oc process -v=POSTGRESQL_DATABASE=${POSTGRESQL_DATABASE},POSTGRESQL_USER=${POSTGRESQL_USERNAME},POSTGRESQL_PASSWORD=${POSTGRESQL_PASSWORD} -f ${SCRIPT_BASE_DIR}/support/templates/postgresql-persistent.json | oc create -n ${IOT_OCP_PROJECT} -f-

echo
echo "Waiting for PostgreSQL to deploy..."
echo
wait_for_application_deployment "${POSTGRESQL_APP_NAME}"


echo
echo "Creating Database Schema..."
echo

POSTGRESQL_POD_NAME=$(oc get pods -l "name=${POSTGRESQL_APP_NAME}" --template='{{ index .items 0 "metadata" "name" }}')

oc rsync "${SCRIPT_BASE_DIR}/support/sql" $POSTGRESQL_POD_NAME:/tmp/

sleep 10

oc rsh -t ${POSTGRESQL_POD_NAME}  bash -c 'psql -f /tmp/sql/postgresql-iot.sql --variable=measureOwner=$POSTGRESQL_USER $POSTGRESQL_DATABASE' 

echo
echo "Deploying AMQ..."
echo
oc process -v=MQ_USERNAME=${MQ_USER},MQ_PASSWORD=${MQ_PASSWORD},IMAGE_STREAM_NAMESPACE=${IOT_OCP_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/amq62-basic.json | oc create -n ${IOT_OCP_PROJECT} -f-

echo
echo "Waiting for AMQ to deploy..."
echo
wait_for_application_deployment "broker-amq"

echo
echo "Exposing MQTT Route..."
echo
oc expose svc -n ${IOT_OCP_PROJECT} broker-amq-mqtt

echo
echo "Deploying Decision Server..."
echo
oc process -v=KIE_SERVER_USER="${KIE_USER}",KIE_SERVER_PASSWORD="${KIE_PASSWORD}",IMAGE_STREAM_NAMESPACE=${IOT_OCP_PROJECT},SOURCE_REPOSITORY_REF=${GIT_BRANCH} -f ${SCRIPT_BASE_DIR}/support/templates/decisionserver63-basic-s2i.json | oc create -n ${IOT_OCP_PROJECT} -f-

validate_build_deploy "kie-app"

echo
echo "Deploying FIS Application..."
echo
oc process -v=KIE_APP_USER="${KIE_USER}",KIE_APP_PASSWORD="${KIE_PASSWORD}",GIT_REF=${GIT_BRANCH},BROKER_AMQ_USERNAME="${MQ_USER}",BROKER_AMQ_PASSWORD="${MQ_PASSWORD}",IMAGE_STREAM_NAMESPACE=${IOT_OCP_PROJECT},POSTGRESQL_USER=${POSTGRESQL_USERNAME},POSTGRESQL_PASSWORD=${POSTGRESQL_PASSWORD},POSTGRESQL_DATABASE=${POSTGRESQL_DATABASE} -f ${SCRIPT_BASE_DIR}/support/templates/fis-generic-template-build.json | oc create -n ${IOT_OCP_PROJECT} -f-

validate_build_deploy "fis-app"

echo
echo "Deploying Software Sensor Application..."
echo
oc process -v=MQTT_USERNAME="${MQ_USER}",MQTT_PASSWORD="${MQ_PASSWORD}",SOURCE_REPOSITORY_REF=${GIT_BRANCH} -f ${SCRIPT_BASE_DIR}/support/templates/software-sensor-template.json | oc create -n ${IOT_OCP_PROJECT} -f-

validate_build_deploy "software-sensor"

echo
echo "Deploying Visualization Tool..."
echo
oc process -v=SOURCE_REPOSITORY_REF=${GIT_BRANCH} -f ${SCRIPT_BASE_DIR}/support/templates/rhel-zeppelin.json | oc create -n ${IOT_OCP_PROJECT} -f-

validate_build_deploy "rhel-zeppelin"

sleep 10

echo
echo "Adding IOT Postgresql Interperter"
echo

# Get Route
ZEPPELIN_ROUTE=$(oc get routes rhel-zeppelin --template='{{ .spec.host }}')

curl -s --fail -H "Content-Type: application/json" -X POST -d "{\"name\":\"iot-ocp\",\"group\":\"psql\",\"properties\":{\"postgresql.password\":\"${POSTGRESQL_PASSWORD}\",\"postgresql.max.result\":\"1000\",\"postgresql.user\":\"${POSTGRESQL_USERNAME}\",\"postgresql.url\":\"jdbc:postgresql://postgresql:5432/iot\",\"postgresql.driver.name\":\"org.postgresql.Driver\"},\"dependencies\":[],\"option\":{\"remote\":true,\"isExistingProcess\":false,\"perNoteSession\":false,\"perNoteProcess\":false},\"propertyValue\":\"\",\"propertyKey\":\"\"}" http://${ZEPPELIN_ROUTE}/api/interpreter/setting

echo
echo
echo "OpenShift IoT Example Project Setup Complete."
echo
