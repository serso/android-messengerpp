package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.ActivityDestroyerController;
import org.solovyev.android.Captcha;
import org.solovyev.android.ResolvedCaptcha;
import org.solovyev.android.messenger.api.ApiError;
import org.solovyev.android.messenger.api.ApiResponseErrorException;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.LoginUserAsyncTask;
import org.solovyev.android.messenger.users.MessengerContactsActivity;
import org.solovyev.android.messenger.view.CaptchaViewBuilder;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.text.StringUtils;
import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:15 PM
 */
public class MessengerLoginActivity extends RoboActivity implements CaptchaViewBuilder.CaptchaEnteredListener {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @NotNull
    private AuthService authService;


    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */

    @NotNull
    private static String REALM = "realm";

    public static void startActivity(@NotNull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerLoginActivity.class);
        result.putExtra(REALM, RoboGuice.getInjector(activity).getInstance(Realm.class).getId());
        activity.startActivity(result);
    }

    @NotNull
    private String realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        if (intent != null) {
            final String realm =intent.getStringExtra(REALM);
            if (realm != null) {
                this.realm = realm;
            } else {
                Log.e(MessengerLoginActivity.class.getSimpleName(), "Login activity started without realm id!");
            }
        } else {
            Log.e(MessengerLoginActivity.class.getSimpleName(), "Login activity started without realm id!");
        }

        setContentView(R.layout.msg_main);

        final ViewGroup content = (ViewGroup) findViewById(R.id.content);
        content.addView(ViewFromLayoutBuilder.newInstance(R.layout.msg_login).build(this), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        final Button registerButton = (Button) content.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessengerRegistrationActivity.startActivity(MessengerLoginActivity.this);
            }
        });

        final Button loginButton = (Button) content.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToLogin(null);
            }
        });

    }

    private void tryToLogin(@Nullable ResolvedCaptcha resolvedCaptcha) {
        final EditText loginInput = (EditText) this.findViewById(R.id.login);
        final EditText passwordInput = (EditText) this.findViewById(R.id.password);

        final String login = loginInput.getText().toString();
        final String password = passwordInput.getText().toString();

        new LoginUserAsyncTask(this, realm) {

            @Override
            protected void onSuccessPostExecute(@Nullable Void result) {
                super.onSuccessPostExecute(result);
                MessengerContactsActivity.startActivity(MessengerLoginActivity.this);
            }

            @Override
            protected void onFailurePostExecute(@NotNull Exception e) {
                if (e instanceof ApiResponseErrorException) {
                    final ApiError apiError = ((ApiResponseErrorException) e).getApiError();
                    final Captcha captcha = apiError.getCaptcha();
                    if (captcha == null) {
                        final String errorDescription = apiError.getErrorDescription();
                        if (!StringUtils.isEmpty(errorDescription)) {
                            Toast.makeText(MessengerLoginActivity.this, errorDescription, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MessengerLoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        new CaptchaViewBuilder(MessengerLoginActivity.this, captcha, MessengerLoginActivity.this).build().show();
                    }
                } else {
                    super.onFailurePostExecute(e);
                }
            }
        }.execute(new LoginUserAsyncTask.Input(login, password, resolvedCaptcha));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ActivityDestroyerController.getInstance().fireActivityDestroyed(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (this.authService.isUserLoggedIn(realm)) {
            MessengerContactsActivity.startActivity(this);
        }
    }

    @Override
    public void onCaptchaEntered(@NotNull ResolvedCaptcha resolvedCaptcha) {
        tryToLogin(resolvedCaptcha);
    }
}
