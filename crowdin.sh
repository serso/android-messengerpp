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

function copyTranslations {
    language=$1
    resourcePostfix=$2

	copyTranslation "translations/$language/strings.xml" "core/res/values$resourcePostfix"
	copyTranslation "translations/$language/raw/*" "core/res/raw$resourcePostfix"
	copyTranslation "translations/$language/sms/*" "realm-sms/res/values$resourcePostfix"
	copyTranslation "translations/$language/vk/*" "realm-vk/res/values$resourcePostfix"
	copyTranslation "translations/$language/xmpp/*" "realm-xmpp/res/values$resourcePostfix"
}


copyTranslations "en" ""
copyTranslations "ru" "-ru"


rm -r translations
