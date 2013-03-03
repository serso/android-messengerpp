package org.solovyev.android.messenger.security;

import android.content.Context;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:03 PM
 */
public interface AuthServiceFacade {

    @Nonnull
    AuthData loginUser(@Nonnull String login,
                       @Nonnull String password,
                       @Nullable ResolvedCaptcha resolvedCaptcha,
                       @Nonnull Context context) throws InvalidCredentialsException;

    @Nonnull
    User getUser(@Nonnull Context context) throws UserIsNotLoggedInException;

    boolean isUserLoggedIn();

    void logoutUser(@Nonnull Context context);

    @Nonnull
    AuthData getAuthData() throws UserIsNotLoggedInException;
}
