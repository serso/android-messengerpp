package org.solovyev.android.messenger.vk.registration;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.registration.RegistrationService;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:23 PM
 */
public class DummyRegistrationService implements RegistrationService {
    @Override
    public void requestVerificationCode(@NotNull String phoneNumber, @NotNull String firstName, @NotNull String lastName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean checkVerificationCode(@NotNull String verificationCode) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
