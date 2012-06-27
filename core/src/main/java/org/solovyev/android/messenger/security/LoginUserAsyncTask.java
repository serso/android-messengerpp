package org.solovyev.android.messenger.security;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.ResolvedCaptcha;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.api.MessengerAsyncTask;

import java.util.List;

/**
 * User: serso
 * Date: 5/29/12
 * Time: 11:45 PM
 */
public abstract class LoginUserAsyncTask extends MessengerAsyncTask<LoginUserAsyncTask.Input, Void, Void> {

    public LoginUserAsyncTask(@NotNull Context context) {
        super(context, true);
    }

    @Override
    protected Void doWork(@NotNull List<Input> params) {
        assert params.size() == 1;
        final Input input = params.get(0);

        final Context context = getContext();
        if (context != null) {
            try {
                getAuthServiceFacade().loginUser(context, input.login, input.password, input.resolvedCaptcha);
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
            MessengerConfigurationImpl.getInstance().getServiceLocator().getSyncService().syncAll(context);
        }
    }

    @NotNull
    private AuthServiceFacade getAuthServiceFacade() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator().getAuthServiceFacade();
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
