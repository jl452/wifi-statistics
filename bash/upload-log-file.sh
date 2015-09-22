#!/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin:$PATH

from=$1
partName=$2
BOUNDARY="---------------------------2123031430233345160473276202"
TEMPFILE=/tmp/import-log.tmp
TEMP_ERROR_CODE=/tmp/error-code.tmp

rm $TEMP_ERROR_CODE
find -L "$from" -name "$partName*" -exec bash -c "echo {} ; sleep 5 ; echo -n --$BOUNDARY$'\r'$'\n'Content-Disposition:\ form-data\;\ name=\\\"importLogFile\\\"\;\ filename=\\\"{}\\\"$'\r'$'\n'Content-Type:\ text/plain$'\r'$'\n'$'\r'$'\n' > $TEMPFILE && cat {} >> $TEMPFILE && echo -n $'\r'$'\n'--$BOUNDARY--$'\r'$'\n' >> $TEMPFILE && echo getTry > $TEMP_ERROR_CODE ; while true ; do cat $TEMP_ERROR_CODE |grep getTry > /dev/null ||exit 0 ; wget --timeout=1800 --tries=10 --waitretry=15 --retry-connrefused --header=\"Content-type: multipart/form-data, boundary=$BOUNDARY\" --post-file=$TEMPFILE http://localhost:8082/wifi-statistics/rest/load -O- && echo ready > $TEMP_ERROR_CODE ; done " \;
grep error $TEMP_ERROR_CODE > /dev/null && exit 1 || exit 0