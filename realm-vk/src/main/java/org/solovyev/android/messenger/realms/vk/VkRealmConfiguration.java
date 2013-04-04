package org.solovyev.android.messenger.realms.vk;

import org.solovyev.android.messenger.realms.RealmConfiguration;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

public class VkRealmConfiguration extends JObject implements RealmConfiguration {

    @Nonnull
    private String login;

    @Nonnull
    private transient String password = "";

    @Nonnull
    private String accessToken;

    // todo serso: implement

    public String getAccessToken() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public VkRealmConfiguration clone() {
        return (VkRealmConfiguration) super.clone();
    }

    @Nonnull
    public String getLogin() {
        return login;
    }

    @Nonnull
    public String getPassword() {
        return password;
    }

    public void setAccessToken(@Nonnull String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserId() {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
