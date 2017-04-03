[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause) [![Build Status](https://travis-ci.org/ethauvin/mobibot.svg?branch=master)](https://travis-ci.org/ethauvin/mobibot)

Some very basic instructions:

```
    { clone with git or download the ZIP }
    git clone git://github.com/ethauvin/mobibot.git

    cd mobibot

    { build with gradle }
    ./gradlew

    cd deploy

    { configure the properties }
    vi *.properties

    { help }
    java -jar mobibot.jar -h

    { twitter oauth token request }
    java -cp mobibot.jar net.thauvin.erik.mobibot.TwitterOAuth <consumerKey> <consumerSecret>

    { launch }
    /usr/bin/nohup java -jar mobibot.jar &
```