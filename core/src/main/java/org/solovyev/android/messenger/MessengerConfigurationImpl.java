package org.solovyev.android.messenger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.Realm;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:36 PM
 */
@Singleton
public class MessengerConfigurationImpl implements MessengerConfiguration {

    @Inject
    @NotNull
    private Realm realm;

    @NotNull
    private MessengerApiProvider messengerApiProvider;

    public MessengerConfigurationImpl() {
    }

    @NotNull
    @Override
    public Realm getRealm() {
        return this.realm;
    }

    public void setMessengerApiProvider(@NotNull MessengerApiProvider messengerApiProvider) {
        this.messengerApiProvider = messengerApiProvider;
    }

    @NotNull
    @Override
    public MessengerApi getMessengerApi() {
        return messengerApiProvider.getMessengerApi();
    }
}
