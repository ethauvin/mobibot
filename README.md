[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause) [![Dependency Status](https://beta.gemnasium.com/badges/github.com/ethauvin/mobibot.svg)](https://beta.gemnasium.com/projects/github.com/ethauvin/mobibot) [![Build Status](https://travis-ci.org/ethauvin/mobibot.svg?branch=master)](https://travis-ci.org/ethauvin/mobibot) [![CircleCI](https://circleci.com/gh/ethauvin/mobibot/tree/master.svg?style=shield)](https://circleci.com/gh/ethauvin/mobibot/tree/master)

Some very basic instructions:

```
    { clone with git or download the ZIP }
    git clone https://github.com/ethauvin/mobibot.git

    cd mobibot

    { build with gradle }
    ./gradlew

    cd deploy

    { configure the properties }
    vi *.properties *.xml

    { help }
    java -jar mobibot.jar -h

    { twitter oauth token request }
    java -cp mobibot.jar net.thauvin.erik.mobibot.TwitterOAuth <consumerKey> <consumerSecret>

    { launch }
    /usr/bin/nohup java -jar mobibot.jar &
```