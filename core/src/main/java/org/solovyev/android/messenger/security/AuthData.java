package org.solovyev.android.messenger.security;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 12:49 AM
 */
public interface AuthData {

    @NotNull
    String getAccessToken();

    @NotNull
    String getRealmUserId();

    @NotNull
    String getRealmUserLogin();
}
