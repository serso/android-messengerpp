package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;
import org.solovyev.common.JCloneable;
import org.solovyev.common.JObject;
import org.solovyev.common.text.Strings;

public class RealmEntityImpl extends JObject implements JCloneable<RealmEntityImpl>, RealmEntity {

    private static final String DELIMITER = "_";

    @NotNull
    private String realmId;

    @NotNull
    private String realmEntityId;

    @NotNull
    private String entityId;

    private RealmEntityImpl(@NotNull String realmId, @NotNull String realmEntityId) {
        this.realmId = realmId;
        this.realmEntityId = realmEntityId;
        this.entityId = realmId + DELIMITER + realmEntityId;
    }

    @NotNull
    public static RealmEntity newInstance(@NotNull String realmId, @NotNull String realmEntityId) {
        if (Strings.isEmpty(realmId)) {
            throw new IllegalArgumentException("Realm cannot be empty!");
        }

        if (Strings.isEmpty(realmEntityId)) {
            throw new IllegalArgumentException("Realm entity id cannot be empty!");
        }

        return new RealmEntityImpl(realmId, realmEntityId);
    }

    @NotNull
    public static RealmEntity fromEntityId(@NotNull String entityId) {
        final int index = entityId.indexOf(DELIMITER);
        if ( index >= 0 ) {
            final String realmId = entityId.substring(0, index);
            final String realmUserId = entityId.substring(index + 1);
            return newInstance(realmId, realmUserId);
        } else {
            throw new IllegalArgumentException("No realm is stored in entityId!");
        }
    }

    @NotNull
    public String getEntityId() {
        return entityId;
    }

    @NotNull
    public String getRealmId() {
        return this.realmId;
    }

    @NotNull
    public String getRealmEntityId() {
        return this.realmEntityId;
    }

    @NotNull
    @Override
    public RealmEntityImpl clone() {
        return (RealmEntityImpl) super.clone();
    }
}
