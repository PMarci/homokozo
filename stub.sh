#!/bin/sh
MYSELF=`which "$0" 2>/dev/null` # get name of command executed (name of this file)
[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0" # if which failed but this file exists, assign ./filename to MYSELF
java=java
if test -n "$JAVA_HOME"; then # if the env var JAVA_HOME has nonzero length, assign $JAVA_HOME/bin/java to java
    java="$JAVA_HOME/bin/java"
fi
exec "$java" $java_args -jar $MYSELF "$@" # run java with the filename of this script as the jar reference,
# and all other arguments passed afterwards
# $java_args appears to be forgotten by the author
exit 1
