#!/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin:$PATH

dirLogsIn=$1
time find -L "$dirLogsIn" -name "log-filtered-wifi-*.log" -exec bash -c "echo '{}' ; MAC=\`echo '{}' \|grep -o \"log-filtered-wifi-.*\.log\" |grep -o \"\-[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}-\" |grep -o \"[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}\"\` ; sed -i \"s/^\([^,]*\),[^,]*,\(.*\)$/\1,\$MAC,\2/\" {}" \;
