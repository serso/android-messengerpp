package org.solovyev.android.messenger.realms.xmpp;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 8:40 PM
 */
public final class TestXmppConfiguration {

    public static final String USER_LOGIN = "messengerplusplus@gmail.com";

    @Nonnull
    private final static XmppRealmConfiguration instance = new XmppRealmConfiguration("talk.google.com", USER_LOGIN, "Qwerty!@");

    private TestXmppConfiguration() {
    }

    @Nonnull
    public static XmppRealmConfiguration getInstance() {
        return instance;
    }
}
