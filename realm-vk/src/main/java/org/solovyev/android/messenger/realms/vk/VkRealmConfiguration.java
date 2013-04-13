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

    @Nonnull
    private String userId;

    public VkRealmConfiguration(@Nonnull String login, @Nonnull String password) {
        this.login = login;
        this.password = password;
    }

    @Nonnull
    public String getAccessToken() {
        return accessToken;
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

    public void setAccessParameters(@Nonnull String accessToken, @Nonnull String userId) {
        this.accessToken = accessToken;
        this.userId = userId;
        // we obtained access token => password is not needed anymore
        this.password = "";
    }


    @Nonnull
    public String getUserId() {
        return userId;
    }
}
