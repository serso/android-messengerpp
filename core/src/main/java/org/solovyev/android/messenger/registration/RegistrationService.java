package org.solovyev.android.messenger.registration;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:08 PM
 */
public interface RegistrationService {

    void requestVerificationCode(@Nonnull String phoneNumber, @Nonnull String firstName, @Nonnull String lastName);

    boolean checkVerificationCode(@Nonnull String verificationCode);
}
