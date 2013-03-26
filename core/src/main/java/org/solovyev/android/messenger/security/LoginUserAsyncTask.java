package org.solovyev.android.messenger.security;

import android.content.Context;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.sync.SyncAllTaskIsAlreadyRunning;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 5/29/12
 * Time: 11:45 PM
 */
public abstract class LoginUserAsyncTask extends MessengerAsyncTask<LoginUserAsyncTask.Input, Void, Void> {

    @Nonnull
    private String realmId;

    public LoginUserAsyncTask(@Nonnull Context context, @Nonnull String realmId) {
        super(context, MaskParams.newDefault());
        this.realmId = realmId;
    }

    @Override
    protected Void doWork(@Nonnull List<Input> params) {
        assert params.size() == 1;
        final Input input = params.get(0);

        final Context context = getContext();
        if (context != null) {
            // todo serso: implement
        }

        return null;
    }

    @Override
    protected void onSuccessPostExecute(@Nullable Void result) {
        final Context context = getContext();
        if (context != null) {
            try {
                MessengerApplication.getServiceLocator().getSyncService().syncAll(false);
            } catch (SyncAllTaskIsAlreadyRunning syncAllTaskIsAlreadyRunning) {
                // do not care
            }
        }
    }

    public static class Input {

        @Nonnull
        private String login;

        @Nonnull
        private String password;

        @Nullable
        private ResolvedCaptcha resolvedCaptcha;

        public Input(@Nonnull String login, @Nonnull String password, @Nullable ResolvedCaptcha resolvedCaptcha) {
            this.login = login;
            this.password = password;
            this.resolvedCaptcha = resolvedCaptcha;
        }
    }
}
