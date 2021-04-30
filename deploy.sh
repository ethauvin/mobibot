#!/bin/bash

DEPLOYDIR=/home/erik/mobitopia/mobibot

if [ -f "deploy/mobibot.jar" ]
then
    /bin/cp deploy/mobibot.jar $DEPLOYDIR
    rm -rf $DEPLOYDIR/lib/*.jar
    cp deploy/lib/*.jar $DEPLOYDIR/lib
    chmod 755 $DEPLOYDIR/*.jar $DEPLOYDIR/lib/*.jar
else
    echo "mobibot.jar not found."
fi
