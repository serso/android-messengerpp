package org.solovyev.android.messenger.vk.secutiry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.RuntimeIoException;
import org.solovyev.android.http.AndroidHttpUtils;
import org.solovyev.android.ResolvedCaptcha;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.User;

import java.io.IOException;

/**
* User: serso
* Date: 5/28/12
* Time: 1:17 PM
*/
public class VkRealmAuthService implements RealmAuthService {

    @NotNull
    @Override
    public AuthData loginUser(@NotNull String login, @NotNull String password, @Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
        try {
            return AndroidHttpUtils.execute(new VkAuthenticationHttpTransaction(login, password, resolvedCaptcha));
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override
    public void logoutUser(@NotNull User user) {
        // do nothing
    }
}
