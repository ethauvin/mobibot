#!/bin/bash

./bld clean jar deploy
[ $? -eq 0 ] && sftp nix4.thauvin.us <<EOF
cd /home/mobibot/mobitopia/mobibot
lcd deploy
put *.jar
cd lib
rm *.jar
put lib/*.jar
EOF
