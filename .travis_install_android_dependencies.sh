#!/bin/sh

# Scripts installs Android SDK artifacts in local Maven repository

git clone git://github.com/serso/maven-android-sdk-deployer.git
cd ./maven-android-sdk-deployer/
git checkout tags/api-19
mvn install -P4.2
cd ..