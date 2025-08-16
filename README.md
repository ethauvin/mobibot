# mobibot

[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.10-7f52ff.svg)](https://kotlinlang.org)
[![bld](https://img.shields.io/badge/2.3.0-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ethauvin_mobibot&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ethauvin_mobibot)
[![GitHub CI](https://github.com/ethauvin/mobibot/actions/workflows/bld.yml/badge.svg)](https://github.com/ethauvin/mobibot/actions/workflows/bld.yml)
[![CircleCI](https://circleci.com/gh/ethauvin/mobibot/tree/master.svg?style=shield)](https://circleci.com/gh/ethauvin/mobibot/tree/master)

Some very basic instructions:

```sh
    # clone with git or download the ZIP
    git clone https://github.com/ethauvin/mobibot.git

    cd mobibot

    # build JAR and deploy
    ./bld jar deploy

    cd deploy

    # configure the properties
    vi *.properties *.xml

    # help
    java -jar mobibot.jar -h
    
    # launch
    /usr/bin/nohup java -jar mobibot.jar &
```

For a listing of features, see the [website](https://mobitopia.org/mobibot/).
