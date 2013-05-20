package org.solovyev.android.messenger.realms.vk;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 1:47 PM
 */
public class VkConfigurationImpl implements VkConfiguration {

	@Nonnull
	private String clientId;

	@Nonnull
	private String clientSecret;

	@Nonnull
	private static final VkConfigurationImpl instance = new VkConfigurationImpl();

	private VkConfigurationImpl() {
	}

	@Nonnull
	public static VkConfigurationImpl getInstance() {
		return instance;
	}


	@Nonnull
	@Override
	public String getClientId() {
		return this.clientId;
	}

	@Nonnull
	@Override
	public String getClientSecret() {
		return this.clientSecret;
	}

	public void setClientId(@Nonnull String clientId) {
		this.clientId = clientId;
	}

	public void setClientSecret(@Nonnull String clientSecret) {
		this.clientSecret = clientSecret;
	}
}
