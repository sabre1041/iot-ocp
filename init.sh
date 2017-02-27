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
AMQ_SSL_PASSWORD="iotocp"
BUILD_CHECK_INTERVAL=5
BUILD_CHECK_TIMES=60
DEPLOYMENT_CHECK_INTERVAL=10
DEPLOYMENT_CHECK_TIMES=60
CURRENT_STAGE=
POSTGRESQL_LABEL="iotphase=postgresql"
AMQ_LABEL="iotphase=amq"
KIE_LABEL="iotphase=kie"
FIS_LABEL="iotphase=fis"
SOFTWARE_SENSOR_LABEL="iotphase=software-sensor"
ZEPPELIN_LABEL="iotphase=zeppelin"
ZEPPELIN_RHEL_BASE_IMAGESTREAM="rhel7:7.2"
ZEPPELIN_CENTOS_BASE_IMAGESTREAM="centos:7"
ZEPPELIN_BASE_IMAGESTREAM=${ZEPPELIN_RHEL_BASE_IMAGESTREAM}
PROJECT_SUFFIX=
ADMIN_ADDL_USERNAME=
SKIP_STEPS=
STAGES=(configure-ocp postgresql amq kie fis software-sensor build-zeppelin configure-zeppelin)

trap exit_message EXIT

function exit_message() {

    exit_code=$?

    if [ ! -z ${CURRENT_STAGE} ]; then
        echo
        echo "Provisioning Failed. Execute \"$0 --restart-from=$CURRENT_STAGE\" Along With Other Provided Parameters to Restart From The Failed Stage"
        echo
    fi

    exit $exit_code
}

# Show script usage
usage() {
  echo "
  Usage: $0 [options]
  Options:
  --zeppelin-base=<rhel7|centos>   : Base used to build Zeppelin Image (Default: rhel7)
  --restart-from=<phase>           : Phase to restart execution
  --project-suffix=<suffix>        : Name of the suffix to apply to a project
  --skip-steps=<steps>             : Comma separated list of steps to skip
  --user=<user>                    : User to add as an Administrator to the project
  -h|--help                        : Show script usage
   "
}

function findIndex() {
    QUERY=$1
    START=-1

    if [ ! -z ${QUERY} ]; then

        for i in "${!STAGES[@]}"; do
            if [[ "${STAGES[$i]}" = "${QUERY}" ]]; then
                START=${i}
            fi
        done

    fi

    echo ${START}
}

function check_restart() {

    if [ ! -z $1 ] && [ "$1" == "restart" ]; then
        return 0
    else
        return 1
    fi

}

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

        if [ "${DEPLOYMENT_VERSION}" == "1" ]; then
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



# Begin Chained Components
function do_configure_ocp() {

    if check_restart $1
    then
        ACTION="replace"
    else
        ACTION="create"
    fi

    CURRENT_STAGE="configure-ocp"
    ZEPPELIN_IMAGE=$(echo ${ZEPPELIN_BASE_IMAGESTREAM} | cut -d: -f1)
    
    echo "Setting up OpenShift IoT Example Project"

    echo
    echo "Creating Project: ${IOT_OCP_PROJECT}..."
    echo
    oc new-project ${IOT_OCP_PROJECT} --description="Showcases an Intelligent Internet-of-Things (IoT) Gateway on Red Hat’s OpenShift Container Platform" --display-name="Internet of Things (IoT) OpenShift Demo Project" >/dev/null 2>&1

    if [ ! -z ${ADMIN_ADDL_USERNAME} ]; then
        echo
        echo "Adding admin role to ${ADMIN_ADDL_USERNAME}..."
        echo

        oc policy add-role-to-user admin ${ADMIN_ADDL_USERNAME} -n ${IOT_OCP_PROJECT}
    fi

    echo
    echo "Creating ImageStreams..."
    echo
    oc $ACTION -n ${IOT_OCP_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/jboss-image-streams.json
    oc $ACTION -n ${IOT_OCP_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/fis-image-streams.json
    oc $ACTION -n ${IOT_OCP_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/openjdk18-openshift-is.json
    oc $ACTION -n ${IOT_OCP_PROJECT} -f ${SCRIPT_BASE_DIR}/support/templates/${ZEPPELIN_IMAGE}-is.json

    echo
    echo "Pausing 10 Seconds..."
    echo
    sleep 10

    echo
    echo "Importing ImageStreams..."
    echo
    oc import-image -n ${IOT_OCP_PROJECT} jboss-decisionserver63-openshift --all=true >/dev/null 2>&1
    oc import-image -n ${IOT_OCP_PROJECT} jboss-amq-62 --all=true >/dev/null 2>&1
    oc import-image -n ${IOT_OCP_PROJECT} fis-karaf-openshift --all=true >/dev/null 2>&1
    oc import-image -n ${IOT_OCP_PROJECT} openjdk18-openshift --all=true >/dev/null 2>&1
    oc import-image -n ${IOT_OCP_PROJECT} ${ZEPPELIN_IMAGE} --all=true >/dev/null 2>&1

}

function do_postgresql() {

    CURRENT_STAGE="postgresql"

    if check_restart $1
    then
        oc delete all -l ${POSTGRESQL_LABEL}
        oc delete pvc -l ${POSTGRESQL_LABEL}
        sleep 15
    fi

    echo
    echo "Deploying PostgreSQL..."
    echo
    oc process -v=POSTGRESQL_DATABASE=${POSTGRESQL_DATABASE} -v=POSTGRESQL_USER=${POSTGRESQL_USERNAME} -v=POSTGRESQL_PASSWORD=${POSTGRESQL_PASSWORD} -l ${POSTGRESQL_LABEL} -f ${SCRIPT_BASE_DIR}/support/templates/postgresql-persistent.json | oc create -n ${IOT_OCP_PROJECT} -f-

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

}

function do_amq() {
    
    CURRENT_STAGE="amq"

    if check_restart $1
    then
        oc delete all -l ${AMQ_LABEL}
        oc delete sa amq-service-account
        oc delete secret amq-app-secret
        sleep 15
    fi

    echo
    echo "Creating AMQ Service Account..."
    echo
    oc create serviceaccount amq-service-account
    oc policy add-role-to-user edit system:serviceaccount:${IOT_OCP_PROJECT}:amq-service-account

    echo
    echo "Creating AMQ Secret..."
    echo
    oc secrets new amq-app-secret ${SCRIPT_BASE_DIR}/support/amq-ssl

    echo
    echo "Deploying AMQ..."
    echo
    oc process -v=MQ_USERNAME=${MQ_USER} -v=MQ_PASSWORD=${MQ_PASSWORD} -v=IMAGE_STREAM_NAMESPACE=${IOT_OCP_PROJECT} -v=AMQ_TRUSTSTORE_PASSWORD=${AMQ_SSL_PASSWORD} -v=AMQ_KEYSTORE_PASSWORD=${AMQ_SSL_PASSWORD} -l ${AMQ_LABEL} -f ${SCRIPT_BASE_DIR}/support/templates/amq62-ssl.json | oc create -n ${IOT_OCP_PROJECT} -f-

    echo
    echo "Waiting for AMQ to deploy..."
    echo
    wait_for_application_deployment "broker-amq"

    echo
    echo "Creating Secure AMQ Route..."
    echo
    oc create route passthrough -n ${IOT_OCP_PROJECT} broker-amq-mqtt --service=broker-amq-tcp-ssl --port=61617

}

function do_kie() {
    
    CURRENT_STAGE="kie"

    if check_restart $1
    then
        oc delete all -l ${KIE_LABEL}
        sleep 15
    fi

    echo
    echo "Deploying Decision Server..."
    echo
    oc process -v=KIE_SERVER_USER="${KIE_USER}" -v=KIE_SERVER_PASSWORD="${KIE_PASSWORD}" -v=IMAGE_STREAM_NAMESPACE=${IOT_OCP_PROJECT} -v=SOURCE_REPOSITORY_REF=${GIT_BRANCH} -l ${KIE_LABEL} -f ${SCRIPT_BASE_DIR}/support/templates/decisionserver63-basic-s2i.json | oc create -n ${IOT_OCP_PROJECT} -f-

    validate_build_deploy "kie-app"

}

function do_fis() {
    
    CURRENT_STAGE="fis"

    if check_restart $1
    then
        oc delete all -l ${FIS_LABEL}
        sleep 15
    fi

    echo
    echo "Deploying FIS Application..."
    echo
    oc process -v=KIE_APP_USER="${KIE_USER}" -v=KIE_APP_PASSWORD="${KIE_PASSWORD}" -v=GIT_REF=${GIT_BRANCH} -v=BROKER_AMQ_USERNAME="${MQ_USER}" -v=BROKER_AMQ_PASSWORD="${MQ_PASSWORD}" -v=IMAGE_STREAM_NAMESPACE=${IOT_OCP_PROJECT} -v=POSTGRESQL_USER=${POSTGRESQL_USERNAME} -v=POSTGRESQL_PASSWORD=${POSTGRESQL_PASSWORD} -v=POSTGRESQL_DATABASE=${POSTGRESQL_DATABASE} -l ${FIS_LABEL} -f ${SCRIPT_BASE_DIR}/support/templates/fis-generic-template-build.json | oc create -n ${IOT_OCP_PROJECT} -f-

    validate_build_deploy "fis-app"

}

function do_software_sensor() {
    
    CURRENT_STAGE="software-sensor"

    if check_restart $1
    then
        oc delete all -l ${SOFTWARE_SENSOR_LABEL}
        sleep 15
    fi

    echo
    echo "Deploying Software Sensor Application..."
    echo
    oc process -v=MQTT_USERNAME="${MQ_USER}" -v=MQTT_PASSWORD="${MQ_PASSWORD}" -v=SOURCE_REPOSITORY_REF=${GIT_BRANCH} -v=IMAGE_STREAM_NAMESPACE=${IOT_OCP_PROJECT} -l ${SOFTWARE_SENSOR_LABEL} -f ${SCRIPT_BASE_DIR}/support/templates/software-sensor-template.json | oc create -n ${IOT_OCP_PROJECT} -f-

    validate_build_deploy "software-sensor"

}

function do_build_zeppelin() {
    
    CURRENT_STAGE="build-zeppelin"

    if check_restart $1
    then
        oc delete all -l ${ZEPPELIN_LABEL}
        oc delete pvc -l ${ZEPPELIN_LABEL}
        sleep 15
    fi
       
    echo
    echo "Deploying Visualization Tool..."
    echo
    oc process -v=SOURCE_REPOSITORY_REF=${GIT_BRANCH} -v=BASE_IMAGESTREAMTAG=${ZEPPELIN_BASE_IMAGESTREAM} -l ${ZEPPELIN_LABEL} -f ${SCRIPT_BASE_DIR}/support/templates/rhel-zeppelin.json | oc create -n ${IOT_OCP_PROJECT} -f-

    validate_build_deploy "rhel-zeppelin"

    sleep 10

}


function do_configure_zeppelin() {
    
    CURRENT_STAGE="configure-zeppelin"
    
    echo
    echo "Adding IOT Postgresql Interperter"
    echo

    # Get Route
    ZEPPELIN_ROUTE=$(oc get routes rhel-zeppelin --template='{{ .spec.host }}')

    ZEPPELIN_STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "Content-Type: application/json" -X POST -d "{\"name\":\"iot-ocp\",\"group\":\"psql\",\"properties\":{\"postgresql.password\":\"${POSTGRESQL_PASSWORD}\",\"postgresql.max.result\":\"1000\",\"postgresql.user\":\"${POSTGRESQL_USERNAME}\",\"postgresql.url\":\"jdbc:postgresql://postgresql:5432/iot\",\"postgresql.driver.name\":\"org.postgresql.Driver\"},\"dependencies\":[],\"option\":{\"remote\":true,\"isExistingProcess\":false,\"perNoteSession\":false,\"perNoteProcess\":false},\"propertyValue\":\"\",\"propertyKey\":\"\"}" http://${ZEPPELIN_ROUTE}/api/interpreter/setting)

    if [ $ZEPPELIN_STATUS -ne 201 ]; then
      echo "Failed to configure zeppelin."  
    fi

}


# Prerequisites
oc whoami >/dev/null 2>&1 || { echo "Cannot validate connectivity to OpenShift. Ensure oc client tool installed and logged in" ; exit 1; }

RESTART_OPTION=

# Process Input
for i in "$@"
do
  case $i in
    --zeppelin-base=*)
      USER_ZEPPELIN_BASE="${i#*=}"
      shift;;
    --restart-from=*)
      RESTART_OPTION="${i#*=}"
      shift;;
    --project-suffix=*)
      IOT_OCP_PROJECT="${IOT_OCP_PROJECT}-${i#*=}"
      shift;;
    --skip-steps=*)
      SKIP_STEPS="${i#*=}"
      shift;;
    --user=*)
      ADMIN_ADDL_USERNAME="${i#*=}"
      shift;;
    -h|--help|*)
      usage
      exit
  esac
done

# Validate Zeppelin Base
if [ ! -z ${USER_ZEPPELIN_BASE} ]; then

    if [ "${USER_ZEPPELIN_BASE}" == "rhel" ]; then
        ZEPPELIN_BASE_IMAGESTREAM=${ZEPPELIN_RHEL_BASE_IMAGESTREAM}
    elif [ "${USER_ZEPPELIN_BASE}" == "centos" ]; then
        ZEPPELIN_BASE_IMAGESTREAM=${ZEPPELIN_CENTOS_BASE_IMAGESTREAM}
    else
        echo "Invalid Zeppelin Base. Either rhel or centos must be provided"
        exit  
    fi
fi

############################################

START_STAGE_INDEX=0

# Determine step to restart from
if [ ! -z ${RESTART_OPTION} ]; then

  # Check position to restart at
  START_STAGE_INDEX=$(findIndex ${RESTART_OPTION})

  if [ $START_STAGE_INDEX -eq -1 ]; then
      echo "Invalid Restart Option: $RESTART_OPTION"
      echo
      echo "Restart Options:"
      for i in "${STAGES[@]}"; do
          echo "   * ${i}" 
      done

      exit 1
  fi

  echo "Restarting at Step: ${RESTART_OPTION}"
  echo

fi

#Run through steps
for step in "${STAGES[@]:${START_STAGE_INDEX}}"
do
    for skipped_step in ${SKIP_STEPS//,/ } ; do
        if [[ "${step}" = "${skipped_step}" ]]; then
            echo "Skipping Step: ${step}"
            echo
            continue 2
        fi
    done

    # Execute Step
    echo "Executing Step: ${step}"
    echo
    
    if [ $step == ${RESTART_OPTION} ]; then
        RESTART_SWITCH="restart"
    else
        RESTART_SWITCH=""
    fi
    
    eval do_${step//-/_} ${RESTART_SWITCH}

done


##############################################

echo "============================================="
echo
echo "OpenShift IoT Example Project Setup Complete."
echo
echo "============================================="
echo

CURRENT_STAGE=
