#!/bin/bash

scale=0.51

names=(Default Nexus-4 Nexus-7)
for name in ${names[*]}
do
     $ANDROID_HOME/tools/monkeyrunner ./check_screenshots.py ../screenshots-new ../screenshots ../screenshots-failed $name
done