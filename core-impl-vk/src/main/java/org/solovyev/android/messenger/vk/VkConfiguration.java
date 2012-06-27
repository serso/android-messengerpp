package org.solovyev.android.messenger.vk;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 1:46 PM
 */
public interface VkConfiguration {

    @NotNull
    String getClientId();

    @NotNull
    String getClientSecret();
}
