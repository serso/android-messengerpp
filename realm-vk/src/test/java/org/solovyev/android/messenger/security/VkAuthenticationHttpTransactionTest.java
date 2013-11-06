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

package org.solovyev.android.messenger.security;

import org.junit.Before;
import org.junit.Test;
import org.solovyev.android.messenger.realms.vk.VkConfigurationImpl;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 10:34 PM
 */
public class VkAuthenticationHttpTransactionTest {

	@Nonnull
	private static final String CLIENT_ID = "2965041";

	@Nonnull
	private static final String CLIENT_SECRET = "hHbJug59sKJie78wjrH8Jdr98gtU";

	@Before
	public void setUp() throws Exception {
		VkConfigurationImpl.getInstance().setClientId(CLIENT_ID);
		VkConfigurationImpl.getInstance().setClientSecret(CLIENT_SECRET);
	}

	@Test
	public void testErrorResult() throws Exception {
		/*try {
			HttpTransactions.execute(new VkAuthenticationHttpTransaction("test", "test"));
            Assert.fail();
        } catch (VkResponseErrorException e) {
            final VkError vkError = e.getApiError();
            // just to be sure that we've got known error
            final VkErrorType vkErrorType = VkErrorType.valueOf(vkError.getErrorId());

            switch (vkErrorType) {
                case invalid_client:
                    break;
                case need_captcha:
                    final Captcha captcha = vkError.getCaptcha();
                    Assert.assertNotNull(captcha);
                    Assert.assertNotNull(captcha.getCaptchaSid());
                    Assert.assertNotNull(captcha.getCaptchaImage());
                    break;
            }
        }*/
	}
}
