#!/bin/bash

./gradlew deploy
[ $? -eq 0 ] && sftp nix3.thauvin.us <<EOF
cd mobitopia/mobibot
lcd deploy
put *.jar
cd lib
rm *.jar
put lib/*.jar
EOF
