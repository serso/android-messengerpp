#!/bin/bash

predefined=1
scale=0.51

# first predefined

if [ $predefined -eq 1 ]
then
    names=(Default Nexus-4 Nexus-7 Nexus-10)
    for name in ${names[*]}
    do
         $ANDROID_HOME/tools/emulator -ports 5580,5581 -avd $name -scale $scale &
         sleep 10
         $ANDROID_HOME/tools/monkeyrunner ./take_screenshots.py ../screenshots $name android-messenger-app.apk
         $ANDROID_HOME/platform-tools/adb -s emulator-5580 emu kill
         sleep 3
    done
fi