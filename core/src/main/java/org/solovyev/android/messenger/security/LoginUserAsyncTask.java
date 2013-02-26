package org.solovyev.android.messenger.security;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.AbstractMessengerApplication;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.sync.SyncAllTaskIsAlreadyRunning;

import java.util.List;

/**
 * User: serso
 * Date: 5/29/12
 * Time: 11:45 PM
 */
public abstract class LoginUserAsyncTask extends MessengerAsyncTask<LoginUserAsyncTask.Input, Void, Void> {

    @NotNull
    private String realmId;

    public LoginUserAsyncTask(@NotNull Context context, @NotNull String realmId) {
        super(context, true);
        this.realmId = realmId;
    }

    @Override
    protected Void doWork(@NotNull List<Input> params) {
        assert params.size() == 1;
        final Input input = params.get(0);

        final Context context = getContext();
        if (context != null) {
            try {
                AbstractMessengerApplication.getServiceLocator().getAuthService().loginUser(realmId, input.login, input.password, input.resolvedCaptcha);
            } catch (InvalidCredentialsException e) {
                throwException(e);
            }
        }

        return null;
    }

    @Override
    protected void onSuccessPostExecute(@Nullable Void result) {
        final Context context = getContext();
        if (context != null) {
            try {
                AbstractMessengerApplication.getServiceLocator().getSyncService().syncAll(context);
            } catch (SyncAllTaskIsAlreadyRunning syncAllTaskIsAlreadyRunning) {
                // do not care
            }
        }
    }

    public static class Input {

        @NotNull
        private String login;

        @NotNull
        private String password;

        @Nullable
        private ResolvedCaptcha resolvedCaptcha;

        public Input(@NotNull String login, @NotNull String password, @Nullable ResolvedCaptcha resolvedCaptcha) {
            this.login = login;
            this.password = password;
            this.resolvedCaptcha = resolvedCaptcha;
        }
    }
}
