package org.solovyev.android.messenger.security;

import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.ResolvedCaptcha;
import org.solovyev.android.messenger.MessengerConfiguration;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 7:01 PM
 */
@Singleton
public class AuthServiceFacadeImpl implements AuthServiceFacade {

    @NotNull
    private final String realm;

    @NotNull
    private final AuthService authService;

    @Inject
    public AuthServiceFacadeImpl(@NotNull MessengerConfiguration configuration,
                                 @NotNull AuthService authService) {
        this.realm = configuration.getRealm().getId();
        this.authService = authService;
    }

    @Override
    @NotNull
    public AuthData loginUser(@NotNull String login, @NotNull String password, @Nullable ResolvedCaptcha resolvedCaptcha, @NotNull Context context) throws InvalidCredentialsException {
        return authService.loginUser(realm, login, password, resolvedCaptcha, context);
    }

    @Override
    @NotNull
    public User getUser(@NotNull Context context) throws UserIsNotLoggedInException {
        return authService.getUser(realm, context);
    }

    @Override
    public boolean isUserLoggedIn() {
        return authService.isUserLoggedIn(realm);
    }

    @Override
    @NotNull
    public AuthData getAuthData() throws UserIsNotLoggedInException {
        return authService.getAuthData(realm);
    }

    @Override
    public void logoutUser(@NotNull Context context) {
        authService.logoutUser(realm, context);
    }
}
