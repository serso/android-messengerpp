/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
