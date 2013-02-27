package org.solovyev.android.messenger.vk.secutiry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.User;

import java.io.IOException;

/**
* User: serso
* Date: 5/28/12
* Time: 1:17 PM
*/
public class VkRealmAuthService implements RealmAuthService {

    @NotNull
    private String login;

    @NotNull
    private String password;

    public VkRealmAuthService(@NotNull String login, @NotNull String password) {
        this.login = login;
        this.password = password;
    }

    @NotNull
    @Override
    public AuthData loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
        try {
            return HttpTransactions.execute(new VkAuthenticationHttpTransaction(login, password, resolvedCaptcha));
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }

    @Override
    public void logoutUser(@NotNull User user) {
        // do nothing
    }
}
