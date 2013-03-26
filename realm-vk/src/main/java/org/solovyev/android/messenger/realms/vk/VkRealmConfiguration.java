package org.solovyev.android.messenger.realms.vk;

import org.solovyev.android.messenger.realms.RealmConfiguration;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

public class VkRealmConfiguration extends JObject implements RealmConfiguration {

    // todo serso: implement

    public String getAccessToken() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public VkRealmConfiguration clone() {
        return (VkRealmConfiguration) super.clone();
    }
}
