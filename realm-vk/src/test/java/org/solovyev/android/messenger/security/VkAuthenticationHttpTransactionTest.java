package org.solovyev.android.messenger.security;

import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.solovyev.android.captcha.Captcha;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.api.CommonApiError;
import org.solovyev.android.messenger.vk.VkConfigurationImpl;
import org.solovyev.android.messenger.vk.http.VkErrorType;
import org.solovyev.android.messenger.vk.http.VkResponseErrorException;
import org.solovyev.android.messenger.vk.secutiry.VkAuthenticationHttpTransaction;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 10:34 PM
 */
public class VkAuthenticationHttpTransactionTest {

    @NotNull
    private static final String CLIENT_ID = "2970921";

    @NotNull
    private static final String CLIENT_SECRET = "Scm7M1vxOdDjpeVj81jw";

    @Before
    public void setUp() throws Exception {
        VkConfigurationImpl.getInstance().setClientId(CLIENT_ID);
        VkConfigurationImpl.getInstance().setClientSecret(CLIENT_SECRET);
    }

    @Test
    public void testErrorResult() throws Exception {
        try {
            HttpTransactions.execute(new VkAuthenticationHttpTransaction("test", "test"));
            Assert.fail();
        } catch (VkResponseErrorException e) {
            final CommonApiError vkError = e.getApiError();
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
        }
    }
}
