package org.solovyev.android.messenger;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 9:32 PM
 */
public interface MessengerApiProvider {

    @NotNull
    MessengerApi getMessengerApi();
}
