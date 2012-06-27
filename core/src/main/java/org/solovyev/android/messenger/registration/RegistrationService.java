package org.solovyev.android.messenger.registration;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:08 PM
 */
public interface RegistrationService {

    void requestVerificationCode(@NotNull String phoneNumber, @NotNull String firstName, @NotNull String lastName);

    boolean checkVerificationCode(@NotNull String verificationCode);
}
