package org.solovyev.android.messenger.realms.vk;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 1:46 PM
 */
public interface VkConfiguration {

    @Nonnull
    String getClientId();

    @Nonnull
    String getClientSecret();
}
