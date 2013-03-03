package org.solovyev.android.messenger.vk.secutiry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nonnull
    private String login;

    @Nonnull
    private String password;

    public VkRealmAuthService(@Nonnull String login, @Nonnull String password) {
        this.login = login;
        this.password = password;
    }

    @Nonnull
    @Override
    public AuthData loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
        try {
            return HttpTransactions.execute(new VkAuthenticationHttpTransaction(login, password, resolvedCaptcha));
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }

    @Override
    public void logoutUser(@Nonnull User user) {
        // do nothing
    }
}
