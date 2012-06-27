package org.solovyev.android.messenger.security;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.ResolvedCaptcha;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:14 PM
 */
public interface AuthService {

    void loginUser(@NotNull Context context, @NotNull String realm, @NotNull String login, @NotNull String password, @Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException;

    /**
     *
     * @param context
     * @param realm realm of logged user
     * @return current logged in user, must be invoked after successful user login
     * @throws IllegalStateException if no successful login has been done
     */
    @NotNull
    User getUser(@NotNull Context context, @NotNull String realm) throws UserIsNotLoggedInException;

    /**
     * @param realm user's realm
     * @return true if user has been logged in
     */
    boolean isUserLoggedIn(@NotNull String realm);

    /**
     * Method logs out user
     *
     * @param context
     * @param realm user's realm
     */
    void logoutUser(@NotNull Context context, @NotNull String realm);


    @NotNull
    AuthData getAuthData(@NotNull String realm) throws UserIsNotLoggedInException;

    void load(@NotNull Context context);

    void save(@NotNull Context context);
}
