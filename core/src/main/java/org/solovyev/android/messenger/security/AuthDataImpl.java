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
    private String userId;

    @Element
    @NotNull
    private String userLogin;

    public AuthDataImpl() {
    }

    public void setAccessToken(@NotNull String accessToken) {
        this.accessToken = accessToken;
    }

    public void setUserId(@NotNull String userId) {
        this.userId = userId;
    }

    public void setUserLogin(@NotNull String userLogin) {
        this.userLogin = userLogin;
    }

    @Override
    @NotNull
    public String getAccessToken() {
        return accessToken;
    }


    @Override
    @NotNull
    public String getUserId() {
        return userId;
    }

    @NotNull
    @Override
    public String getUserLogin() {
        return this.userLogin;
    }
}
