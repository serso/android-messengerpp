package org.solovyev.android.messenger.vk.registration;

import com.google.inject.Singleton;
import javax.annotation.Nonnull;
import org.solovyev.android.messenger.registration.RegistrationService;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:23 PM
 */

@Singleton
public class DummyRegistrationService implements RegistrationService {

    @Override
    public void requestVerificationCode(@Nonnull String phoneNumber, @Nonnull String firstName, @Nonnull String lastName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean checkVerificationCode(@Nonnull String verificationCode) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
