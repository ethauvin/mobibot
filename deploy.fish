#!/usr/bin/env fish

set nix4 'erik@nix4'
set loc /home/mobibot/mobitopia/mobibot
set mobibot 'mobibot.jar'

./bld clean jar deploy
if test $status -eq 0
    echo "Deleting old jars..."
    ssh $nix4 "rm -rf $loc/$mobibot \"$loc/lib/*.jar\""
    echo "Uploading new jars..."
    mscp deploy/lib/*.jar $nix4:$loc/lib/
    scp deploy/$mobibot $nix4:$loc
    echo "Updating permissions..."
    ssh $nix4 "chmod 755 $loc/$mobibot"
end
