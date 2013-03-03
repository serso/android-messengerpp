package org.solovyev.android.messenger.security;

import javax.annotation.Nonnull;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
* User: serso
* Date: 5/30/12
* Time: 12:47 AM
*/
@Root
public class AuthDataImpl implements AuthData {

    @Element
    @Nonnull
    private String accessToken;

    @Element
    @Nonnull
    private String realmUserId;

    @Element
    @Nonnull
    private String realmUserLogin;

    public AuthDataImpl() {
    }

    public void setAccessToken(@Nonnull String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRealmUserId(@Nonnull String realmUserId) {
        this.realmUserId = realmUserId;
    }

    public void setRealmUserLogin(@Nonnull String realmUserLogin) {
        this.realmUserLogin = realmUserLogin;
    }

    @Override
    @Nonnull
    public String getAccessToken() {
        return accessToken;
    }


    @Override
    @Nonnull
    public String getRealmUserId() {
        return realmUserId;
    }

    @Nonnull
    @Override
    public String getRealmUserLogin() {
        return this.realmUserLogin;
    }
}
