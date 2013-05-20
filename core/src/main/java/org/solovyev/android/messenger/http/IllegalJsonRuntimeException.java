package org.solovyev.android.messenger.http;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 10:18 PM
 */
public class IllegalJsonRuntimeException extends RuntimeException {

	@Nonnull
	private final IllegalJsonException illegalJsonException;

	public IllegalJsonRuntimeException(@Nonnull IllegalJsonException e) {
		super(e);
		illegalJsonException = e;
	}

	@Nonnull
	public IllegalJsonException getIllegalJsonException() {
		return illegalJsonException;
	}
}
