#!/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin:$PATH

dirLogsIn="/data/projects/wifi/logs-filtered/08:60:6e:d3:88:54_2/"
##dirLogsIn="/home/user/"
dirLogsOut="/data/projects/wifi/logs-filtered/tmp/"
##dirLogsOut="/mnt/usbdrive/wifi-statistics/"
cd $dirLogsIn
time find -L "." -name "log-filtered-wifi-*.log" -mtime +1 -exec bash -c "MAC=\`echo '{}' \|grep -o \"log-filtered-wifi-.*\.log\" |grep -o \"\-[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}-\" |grep -o \"[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}\"\` ; mkdir --parents `echo $dirLogsOut |sed s/://g`/\`echo \$MAC |sed s/://g\` ; TEMP=\`echo '{}' \|grep -o \"log-filtered-wifi-.*\.log\" |grep -o \"[0-9a-zA-Z]\{2\}:[^.]*\.[^.]*\"\` ; tar -cv --lzma --remove-files -C $dirLogsIn log-filtered-wifi-\$TEMP.log -f`echo $dirLogsOut |sed s/://g`/\`echo \$MAC |sed s/://g\`/log-filtered-wifi-\`echo \$TEMP |sed s/://g\`.log.tar.lzma" \;
