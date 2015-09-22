#!/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin:$PATH

echo wifi.sh 0 $(date)

if [ "$#" -ge 2 ]; then
	mac=$(LANG=C ifconfig -a |grep -m 1 "Link encap:Ethernet" |grep HWaddr |grep -oe "[^ ]*[[:space:]]*$" |grep -oe "[^ ]*")
	ifconfig $1 down
	ifconfig $1 promisc #Set your wifi controller to monitor mode. (not 100% sure if this is necessary but i also enabled the promiscuous mode)
	iwconfig $1 mode monitor #sudo iw $1 type monitor
	ifconfig $1 up
#	tcpdump -i $1 -tt -e -s 256 type mgt subtype probe-resp or subtype probe-req >> $2/log-wifi-$mac-`date +%F.%T`.log
	stdbuf -o0 tcpdump -i $1 -tt -e -s 256 type mgt subtype probe-resp or subtype probe-req |grep -o "^[0-9]*.* -[0-9]*dB .* BSSID:[^ ]*.* DA:\([0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}\|Broadcast\).* SA:\([0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}\|Broadcast\).* \(Probe Request\|Probe Response\).*" |sed "s/^\([0-9]*\).* \(-[0-9]*\)dB .* \(BSSID:[^ ]*\).* DA:\([0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}\|Broadcast\).* SA:\([0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}\|Broadcast\).* \(Probe [A-Za-z]*\)\( (.*)\|\).*$/\1,$mac,\5,\4,\2,\3,\6,_\7_/" |sed "s/,_ (\(.*\))_$/,\1/" |sed "s/,__$/,/" >> $2/log-filtered-wifi-$mac-`date +%F.%T`.log &

##dirLogsIn="/data/projects/wifi/logs-test/1/"
dirLogsIn="/home/user/"
#dirLogsOutTemp="/tmp"
##dirLogsOut="/data/projects/wifi/logs-test/1/"
dirLogsOut="/home/user/"
	find -L "$dirLogsIn" -name "log-wifi-*.log" -exec bash -c "cat '{}' |grep -o \"^[0-9]*.* -[0-9]*dB .* BSSID:[^ ]*.* DA:\([0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}\|Broadcast\).* SA:\([0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}\|Broadcast\).* \(Probe Request\|Probe Response\).*\" |sed \"s/^\([0-9]*\).* \(-[0-9]*\)dB .* \(BSSID:[^ ]*\).* DA:\([0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}\|Broadcast\).* SA:\([0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}\|Broadcast\).* \(Probe [A-Za-z]*\)\( (.*)\|\).*$/\1,\`echo '{}' \|grep -o \"log-filtered-wifi-.*\.log\" |grep -o \"\-[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}-\" |grep -o \"[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}:[0-9a-zA-Z]\{2\}\"\`,\5,\4,\2,\3,\6,_\7_/\" |sed \"s/,_ (\(.*\))_$/,\1/\" |sed \"s/,__$/,/\" |sort |uniq > $dirLogsOut/log-filtered-wifi-\`echo \"{}\" |grep -o \"log-wifi-.*\.log\" |grep -o \"[0-9a-zA-Z]\{2\}:[^.]*\.[^.]*\"\`.log ; rm {}" \;
else
	echo ERROR: need 2 paramtr \(wlanNember /dir/to/write/log/\)
fi

echo wifi.sh = $(date)

exit 0
