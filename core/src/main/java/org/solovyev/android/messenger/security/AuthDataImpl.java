package org.solovyev.android.messenger.security;

import org.jetbrains.annotations.NotNull;
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
    @NotNull
    private String accessToken;

    @Element
    @NotNull
    private String realmUserId;

    @Element
    @NotNull
    private String realmUserLogin;

    public AuthDataImpl() {
    }

    public void setAccessToken(@NotNull String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRealmUserId(@NotNull String realmUserId) {
        this.realmUserId = realmUserId;
    }

    public void setRealmUserLogin(@NotNull String realmUserLogin) {
        this.realmUserLogin = realmUserLogin;
    }

    @Override
    @NotNull
    public String getAccessToken() {
        return accessToken;
    }


    @Override
    @NotNull
    public String getRealmUserId() {
        return realmUserId;
    }

    @NotNull
    @Override
    public String getRealmUserLogin() {
        return this.realmUserLogin;
    }
}
