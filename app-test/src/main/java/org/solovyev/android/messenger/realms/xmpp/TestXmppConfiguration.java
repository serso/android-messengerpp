package org.solovyev.android.messenger.realms.xmpp;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 8:40 PM
 */
public final class TestXmppConfiguration {

    public static final String USER_LOGIN = "messengerplusplus@gmail.com";
    public static final String USER_LOGIN2 = "messengerplusplus2@gmail.com";

    @Nonnull
    private final static XmppRealmConfiguration instance = new XmppRealmConfiguration("talk.google.com", USER_LOGIN, "Qwerty!@");

    @Nonnull
    private final static XmppRealmConfiguration instance2 = new XmppRealmConfiguration("talk.google.com", USER_LOGIN2, "Qwerty!@");

    private TestXmppConfiguration() {
    }

    @Nonnull
    public static XmppRealmConfiguration getInstance() {
        return instance;
    }

    @Nonnull
    public static XmppRealmConfiguration getInstance2() {
        return instance2;
    }
}
