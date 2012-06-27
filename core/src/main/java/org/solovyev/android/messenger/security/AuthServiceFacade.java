package org.solovyev.android.messenger.security;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.ResolvedCaptcha;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:03 PM
 */
public interface AuthServiceFacade {

    void loginUser(@NotNull Context context, @NotNull String login, @NotNull String password, @Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException;

    /**
     * @return current logged in user, must be invoked after successful user login
     * @throws IllegalStateException if no successful login has been done
     * @param context
     */
    @NotNull
    User getUser(@NotNull Context context) throws UserIsNotLoggedInException;

    @NotNull
    AuthData getAuthData() throws UserIsNotLoggedInException;

    /**
     * @return true if user has been logged in
     */
    boolean isUserLoggedIn();

    /**
     * Method logs out user
     * @param context
     */
    void logoutUser(@NotNull Context context);
}
