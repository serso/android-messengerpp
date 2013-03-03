package org.solovyev.android.messenger.security;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 12:49 AM
 */
public interface AuthData {

    @Nonnull
    String getAccessToken();

    @Nonnull
    String getRealmUserId();

    @Nonnull
    String getRealmUserLogin();
}
