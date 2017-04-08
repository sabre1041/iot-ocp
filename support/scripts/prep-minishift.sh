#!/bin/bash

SCRIPT_BASE_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )


echo "
  sudo mkdir -p /var/lib/minishift/pv/pv0{1..3}
  sudo chmod -R 777 /var/lib/minishift/pv/pv*
  sudo chmod -R a+w /var/lib/minishift/pv/pv*
  sudo chcon -R -t svirt_sandbox_file_t /var/lib/minishift/pv/*
  sudo restorecon -R /var/lib/minishift/pv/
  " | minishift ssh
  
for i in $(seq 1 3); do
 
oc create -f - <<PV
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv0$i
spec:
  accessModes:
  - ReadWriteOnce
  - ReadWriteMany
  - ReadOnlyMany
  capacity:
    storage: 2Gi
  hostPath:
    path: /var/lib/minishift/pv/pv0$i
  persistentVolumeReclaimPolicy: Recycle
PV
 
done
