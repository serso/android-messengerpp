package org.solovyev.android.messenger.http;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 10:18 PM
 */
public class IllegalJsonRuntimeException extends RuntimeException {

    @NotNull
    private final IllegalJsonException illegalJsonException;

    public IllegalJsonRuntimeException(@NotNull IllegalJsonException e) {
        super(e);
        illegalJsonException = e;
    }

    @NotNull
    public IllegalJsonException getIllegalJsonException() {
        return illegalJsonException;
    }
}
