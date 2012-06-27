package org.solovyev.android.messenger.security;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.ResolvedCaptcha;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:03 PM
 */
public class AuthServiceFacadeImpl implements AuthServiceFacade {

    @Override
    public void loginUser(@NotNull Context context, @NotNull String login, @NotNull String password, @Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
        getAuthService().loginUser(context, getRealm(), login, password, resolvedCaptcha);
    }

    @NotNull
    private String getRealm() {
        return MessengerConfigurationImpl.getInstance().getRealm();
    }

    @NotNull
    private AuthService getAuthService() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator().getAuthService();
    }

    @NotNull
    @Override
    public User getUser(@NotNull Context context) throws UserIsNotLoggedInException {
        return getAuthService().getUser(context, getRealm());
    }

    @NotNull
    @Override
    public AuthData getAuthData() throws UserIsNotLoggedInException {
        return getAuthService().getAuthData(getRealm());
    }

    @Override
    public boolean isUserLoggedIn() {
        return getAuthService().isUserLoggedIn(getRealm());
    }

    @Override
    public void logoutUser(@NotNull Context context) {
        getAuthService().logoutUser(context, getRealm());
    }
}
