#!/bin/bash

rm -r translations
unzip messengerpp.zip -d translations

function copyTranslation {
    from=$1
    to=$2

	if [ ! -d $to ]; then
    	# if directory doesn't exist create it
    	mkdir $to
    fi

    cp $from $to
}

copyTranslation "translations/en/strings.xml" "core/res/values"
copyTranslation "translations/en/raw/*" "core/res/raw"
copyTranslation "translations/en/sms/*" "realm-sms/res/values"
copyTranslation "translations/en/vk/*" "realm-vk/res/values"
copyTranslation "translations/en/xmpp/*" "realm-xmpp/res/values"


rm -r translations
