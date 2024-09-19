#!/usr/bin/env fish

./bld clean jar deploy
if test $status -eq 0
    echo "cd /home/mobibot/mobitopia/mobibot
lcd deploy
put *.jar
cd lib
rm *.jar
put lib/*.jar" | sftp nix4
end
