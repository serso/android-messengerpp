package org.solovyev.android.messenger.realms;

public class RealmRuntimeException extends RuntimeException {

	public RealmRuntimeException() {
	}

	public RealmRuntimeException(String detailMessage) {
		super(detailMessage);
	}

	public RealmRuntimeException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public RealmRuntimeException(Throwable throwable) {
		super(throwable);
	}
}
