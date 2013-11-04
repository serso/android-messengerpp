#!/bin/sh

# Script installs Android SDK

sudo apt-get update -q
if [ `uname -m` = x86_64 ]; then sudo apt-get install -qq libstdc++6:i386 lib32z1; fi
wget -O android-sdk.tgz http://dl.google.com/android/android-sdk_r22.0.4-linux.tgz
tar xzf android-sdk.tgz

export ANDROID_HOME=$PWD/android-sdk-linux
export PATH=${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools

sudo apt-get install expect
chmod +x $PWD/.travis_install_android_sdk.sh
$PWD/.travis_install_android_sdk.sh
