#!/usr/bin/expect -f

# Script installs Android SDK components

spawn android update sdk --filter tools,platform-tools,build-tools-19.0.0,extra-android-support,android-17,sysimg-17,addon-google_apis-google-17,android-19,sysimg-19,addon-google_apis-google-19,addon-google_apis-google-19,extra-google-play_billing,extra-google-m2repository,extra-google-analytics_sdk_v2,extra-google-gcm,extra-google-google_play_services,extra-google-play_apk_expansion,extra-android-m2repository --no-ui --force --all
expect "Do you accept the license *:"
send -- "y\r"
expect "Do you accept the license *:"
send -- "y\r"
expect "Do you accept the license *:"
send -- "y\r"
expect "Do you accept the license *:"
send -- "y\r"
expect "Do you accept the license *:"
send -- "y\r"
expect "Do you accept the license *:"
send -- "y\r"
expect "Do you accept the license *:"
send -- "y\r"
interact

# WORKAROUND: for some reason we need to download following extras separately (otherwise we will get PkgVersion=2 instead of PkgVersion=2.0.0)
spawn android update sdk --filter extra-google-admob_ads_sdk,extra-google-play_licensing --no-ui --force --all
expect "Do you accept the license *:"
send -- "y\r"
interact
