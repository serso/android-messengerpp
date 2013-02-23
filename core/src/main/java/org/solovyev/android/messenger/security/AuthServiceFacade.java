package org.solovyev.android.messenger.security;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:03 PM
 */
public interface AuthServiceFacade {

    @NotNull
    AuthData loginUser(@NotNull String login,
                       @NotNull String password,
                       @Nullable ResolvedCaptcha resolvedCaptcha,
                       @NotNull Context context) throws InvalidCredentialsException;

    @NotNull
    User getUser(@NotNull Context context) throws UserIsNotLoggedInException;

    boolean isUserLoggedIn();

    void logoutUser(@NotNull Context context);

    @NotNull
    AuthData getAuthData() throws UserIsNotLoggedInException;
}
