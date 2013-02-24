package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;
import org.solovyev.common.JObject;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 4:11 PM
 */
public class RealmUserImpl extends JObject implements RealmUser {

    @NotNull
    private String realmId;

    @NotNull
    private String realmUserId;

    private RealmUserImpl(@NotNull String realmId, @NotNull String realmUserId) {
        this.realmId = realmId;
        this.realmUserId = realmUserId;
    }

    @NotNull
    public static RealmUser newInstance(@NotNull String realmId, @NotNull String realmUserId) {
        return new RealmUserImpl(realmId, realmUserId);
    }

    @NotNull
    @Override
    public String getUserId() {
        return realmId + "_" + realmUserId;
    }

    @NotNull
    @Override
    public String getRealmId() {
        return this.realmId;
    }

    @NotNull
    @Override
    public String getRealmUserId() {
        return this.realmUserId;
    }

    @NotNull
    @Override
    public RealmUserImpl clone() {
        return (RealmUserImpl) super.clone();
    }
}
