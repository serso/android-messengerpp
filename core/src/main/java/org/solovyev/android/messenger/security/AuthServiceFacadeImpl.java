package org.solovyev.android.messenger.security;

import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.MessengerConfiguration;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 7:01 PM
 */
@Singleton
public class AuthServiceFacadeImpl implements AuthServiceFacade {

    @Nonnull
    private final String realm = "";

    @Nonnull
    private final AuthService authService;

    @Inject
    public AuthServiceFacadeImpl(@Nonnull MessengerConfiguration configuration,
                                 @Nonnull AuthService authService) {
        //this.realm = configuration.getRealmDefs().getId();
        this.authService = authService;
    }

    @Override
    @Nonnull
    public AuthData loginUser(@Nonnull String login, @Nonnull String password, @Nullable ResolvedCaptcha resolvedCaptcha, @Nonnull Context context) throws InvalidCredentialsException {
        return authService.loginUser(realm, login, password, resolvedCaptcha);
    }

    @Override
    @Nonnull
    public User getUser(@Nonnull Context context) throws UserIsNotLoggedInException {
        return authService.getUser(realm);
    }

    @Override
    public boolean isUserLoggedIn() {
        return authService.isUserLoggedIn(realm);
    }

    @Override
    @Nonnull
    public AuthData getAuthData() throws UserIsNotLoggedInException {
        return authService.getAuthData(realm);
    }

    @Override
    public void logoutUser(@Nonnull Context context) {
        authService.logoutUser(realm);
    }
}
