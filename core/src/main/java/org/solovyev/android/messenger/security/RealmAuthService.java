package org.solovyev.android.messenger.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.ResolvedCaptcha;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:34 PM
 */
public interface RealmAuthService {

    /**
     *
     * @param login user's login
     * @param password user's password
     * @param resolvedCaptcha entered by user captcha, if no captcha is needed - null
     * @return logged in user
     * @throws InvalidCredentialsException exception if either login or password is incorrect or user with specified login was not found
     */
    @NotNull
    AuthData loginUser(@NotNull String login,
                       @NotNull String password,
                       @Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException;

    void logoutUser(@NotNull User user);
}
