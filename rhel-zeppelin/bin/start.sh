#!/bin/bash


# Check if server previously configured

if [ ! -d "$ZEPPELIN_CONF_DIR" ] && [ ! -d "$ZEPPELIN_NOTEBOOK_DIR" ] && [ ! -d "$ZEPPELIN_LOG_DIR" ]  && [ ! -d "$ZEPPELIN_INTERPRETER_DIR" ]; then
    
    echo "Storage directories do not exist. Creating..."
    
    mkdir -p "${ZEPPELIN_CONF_DIR}" "${ZEPPELIN_NOTEBOOK_DIR}" "${ZEPPELIN_LOG_DIR}" "${ZEPPELIN_INTERPRETER_DIR}"
    
    echo "Copying configuration files..."
    
    cp ${ZEPPELIN_SERVER_HOME}/conf/* ${ZEPPELIN_CONF_DIR}
    
    echo "Copying interpreters..."
    cp -R ${ZEPPELIN_SERVER_HOME}/interpreter/* ${ZEPPELIN_INTERPRETER_DIR}
    
    echo "Installing postgresql interpreter..."
    
    # Install Postgresql Interpreter
    ${ZEPPELIN_SERVER_HOME}/bin/install-interpreter.sh --name postgresql
    
fi

# Start Zeppelin Server
exec ${ZEPPELIN_SERVER_HOME}/bin/zeppelin.sh $@