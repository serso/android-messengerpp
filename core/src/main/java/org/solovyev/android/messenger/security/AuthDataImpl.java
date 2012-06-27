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
    private Integer expiresIn;

    @Element
    @NotNull
    private Integer userId;

    public AuthDataImpl() {
    }

    public void setAccessToken(@NotNull String accessToken) {
        this.accessToken = accessToken;
    }

    public void setExpiresIn(@NotNull Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setUserId(@NotNull Integer userId) {
        this.userId = userId;
    }

    @Override
    @NotNull
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    @NotNull
    public Integer getExpiresIn() {
        return expiresIn;
    }

    @Override
    @NotNull
    public Integer getUserId() {
        return userId;
    }
}
