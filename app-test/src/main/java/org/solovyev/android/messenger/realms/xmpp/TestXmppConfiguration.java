package org.solovyev.android.messenger.realms.xmpp;

import javax.annotation.Nonnull;

public final class TestXmppConfiguration {

	public static final String USER_LOGIN = "messengerplusplus@gmail.com";
	public static final String USER_LOGIN2 = "messengerplusplus2@gmail.com";

	@Nonnull
	private final static XmppAccountConfiguration instance = new XmppAccountConfiguration("talk.google.com", USER_LOGIN, "Qwerty!@");

	@Nonnull
	private final static XmppAccountConfiguration instance2 = new XmppAccountConfiguration("talk.google.com", USER_LOGIN2, "Qwerty!@");

	private TestXmppConfiguration() {
	}

	@Nonnull
	public static XmppAccountConfiguration getInstance() {
		return instance;
	}

	@Nonnull
	public static XmppAccountConfiguration getInstance2() {
		return instance2;
	}
}
