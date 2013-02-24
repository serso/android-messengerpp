package org.solovyev.android.messenger;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.vk.VkConfigurationImpl;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:48 PM
 */
public class MessengerApplication extends AbstractMessengerApplication {

    @NotNull
    public static final String CLIENT_ID = "2970921";

    @NotNull
    public static final String CLIENT_SECRET = "Scm7M1vxOdDjpeVj81jw";

    @NotNull
    public static final String DB_NAME = "mpp";
    public static final int DB_VERSION = 1;


    @Override
    public void onCreate() {
        super.onCreate();

        VkConfigurationImpl.getInstance().setClientId(CLIENT_ID);
        VkConfigurationImpl.getInstance().setClientSecret(CLIENT_SECRET);
    }
}

