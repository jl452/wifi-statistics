#!/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin:$PATH

PATH_SCRIPTS="/data/projects/wifi/wifi-statistics/bash/"
dirLogsIn=$1
dirLogsOutTemp="./tmp"
partName=".log-filtered-wifi-part."
mkdir --parents $dirLogsOutTemp

time find -L "$dirLogsIn" -name "log-filtered-wifi-*.log" -exec bash -c "echo {} ; rm $dirLogsOutTemp/$partName* ; TEMP=\$(echo {} |grep -o "/.*\.[0-9a-zA-Z]*$" |grep -o "[^/]*$") ; echo _ $TEMP _ ; split -l 50000 {} $dirLogsOutTemp/$TEMP$partName ; bash $PATH_SCRIPTS/upload-log-file.sh \"$dirLogsOutTemp\" \"$partName\" && mv {} {}.imported " \;
rm $dirLogsOutTemp/$partName*