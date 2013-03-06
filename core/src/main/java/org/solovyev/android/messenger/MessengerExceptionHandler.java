package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 4:50 PM
 */
public interface MessengerExceptionHandler {

    void handleException(@Nonnull Throwable e);
}
