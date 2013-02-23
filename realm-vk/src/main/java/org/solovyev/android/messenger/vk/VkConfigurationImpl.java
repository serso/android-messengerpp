package org.solovyev.android.messenger.vk;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 1:47 PM
 */
public class VkConfigurationImpl implements VkConfiguration {

    @NotNull
    private String clientId;

    @NotNull
    private String clientSecret;

    @NotNull
    private static final VkConfigurationImpl instance = new VkConfigurationImpl();

    private VkConfigurationImpl() {
    }

    @NotNull
    public static VkConfigurationImpl getInstance() {
        return instance;
    }


    @NotNull
    @Override
    public String getClientId() {
        return this.clientId;
    }

    @NotNull
    @Override
    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientId(@NotNull String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(@NotNull String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
