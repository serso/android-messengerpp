package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.registration.RegistrationService;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.text.Strings;
import roboguice.activity.RoboActivity;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:15 PM
 */
public class MessengerRegistrationActivity extends RoboActivity {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @NotNull
    private RegistrationService registrationService;

    public static void startActivity(@NotNull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerRegistrationActivity.class);
        activity.startActivity(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.msg_main);

        final ViewGroup content = (ViewGroup) findViewById(android.R.id.content);
        content.addView(ViewFromLayoutBuilder.newInstance(R.layout.msg_register).build(this), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        final Button loginButton = (Button) content.findViewById(R.id.next_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText verificationCodeInput = (EditText) content.findViewById(R.id.verification_code);
                final String verificationCode = verificationCodeInput.getText().toString();

                if (Strings.isEmpty(verificationCode)) {
                    // no verification code => start registration procedure
                    final EditText phoneNumberInput = (EditText) content.findViewById(R.id.phone_number);
                    final EditText firstNameInput = (EditText) content.findViewById(R.id.first_name);
                    final EditText lastNameInput = (EditText) content.findViewById(R.id.last_name);

                    final String phoneNumber = phoneNumberInput.getText().toString();
                    final String firstName = firstNameInput.getText().toString();
                    final String lastName = lastNameInput.getText().toString();

                    registrationService.requestVerificationCode(phoneNumber, firstName, lastName);
                } else {
                    registrationService.checkVerificationCode(verificationCode);
                }
            }
        });

    }
}
