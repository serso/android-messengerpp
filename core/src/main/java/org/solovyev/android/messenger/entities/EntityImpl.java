package org.solovyev.android.messenger.entities;

import android.os.Parcel;
import org.solovyev.common.JCloneable;
import org.solovyev.common.JObject;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityImpl extends JObject implements JCloneable<EntityImpl>, Entity {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    public static final String DELIMITER = ":";
    public static final String DELIMITER_REALM = "~";

    public static final Creator <Entity> CREATOR = new Creator<Entity>() {
        @Override
        public Entity createFromParcel(@Nonnull Parcel in) {
            return fromParcel(in);
        }

        @Override
        public Entity[] newArray(int size) {
            return new Entity[size];
        }
    };

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    @Nonnull
    private String realmId;

    @Nullable
    private String realmDefId;

    @Nonnull
    private String realmEntityId;

    @Nullable
    private String appRealmEntityId;

    @Nonnull
    private String entityId;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

    private EntityImpl(@Nonnull String realmId, @Nonnull String realmEntityId, @Nonnull String entityId) {
        this.realmId = realmId;
        this.realmEntityId = realmEntityId;
        this.entityId = entityId;
    }

    private EntityImpl(@Nonnull String realmId,
                       @Nullable String realmDefId,
                       @Nonnull String realmEntityId,
                       @Nonnull String entityId) {
        this.realmId = realmId;
        this.realmDefId = realmDefId;
        this.realmEntityId = realmEntityId;
        this.entityId = entityId;
    }

    @Nonnull
    public static EntityImpl newInstance(@Nonnull String realmId, @Nonnull String realmEntityId, @Nonnull String entityId) {
        if (Strings.isEmpty(realmId)) {
            throw new IllegalArgumentException("Realm cannot be empty!");
        }

        if (Strings.isEmpty(realmEntityId)) {
            throw new IllegalArgumentException("Realm entity id cannot be empty!");
        }

        if (Strings.isEmpty(entityId)) {
            throw new IllegalArgumentException("Entity id cannot be empty!");
        }

        return new EntityImpl(realmId, realmEntityId, entityId);
    }

    @Nonnull
    public static Entity newInstance(@Nonnull String realmId, @Nonnull String realmEntityId) {
        return newInstance(realmId, realmEntityId, realmId + DELIMITER + realmEntityId);
    }

    @Nonnull
    public static Entity fromEntityId(@Nonnull String entityId) {
        final int index = entityId.indexOf(DELIMITER);
        if ( index >= 0 ) {
            final String realmId = entityId.substring(0, index);
            final String realmUserId = entityId.substring(index + 1);
            return newInstance(realmId, realmUserId);
        } else {
            throw new IllegalArgumentException("No realm is stored in entityId!");
        }
    }

    @Nonnull
    private static Entity fromParcel(@Nonnull Parcel in) {
        return new EntityImpl(in.readString(), in.readString(), in.readString(), in.readString());
    }

    @Nonnull
    public String getEntityId() {
        return entityId;
    }

    @Nonnull
    public String getRealmId() {
        return this.realmId;
    }

    @Nonnull
    @Override
    public String getRealmDefId() {
        if ( this.realmDefId == null ) {
            final int index = realmId.indexOf(DELIMITER_REALM);
            if ( index >= 0 ) {
                this.realmDefId = entityId.substring(0, index);
            } else {
                throw new IllegalArgumentException("No realm def id is stored in realmId!");
            }

        }
        return this.realmDefId;
    }

    @Nonnull
    public String getRealmEntityId() {
        return this.realmEntityId;
    }

    @Nonnull
    @Override
    public String getAppRealmEntityId() {
        if (appRealmEntityId == null) {
            final int index = entityId.indexOf(DELIMITER);
            if ( index >= 0 ) {
                appRealmEntityId = entityId.substring(index + 1);
            } else {
                throw new IllegalArgumentException("No realm is stored in entityId!");
            }
        }

        return appRealmEntityId;
    }

    @Nonnull
    @Override
    public EntityImpl clone() {
        return (EntityImpl) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityImpl)) return false;

        EntityImpl that = (EntityImpl) o;

        if (!entityId.equals(that.entityId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return entityId.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@Nonnull Parcel out, int flags) {
        out.writeString(realmId);
        out.writeString(realmDefId);
        out.writeString(realmEntityId);
        out.writeString(entityId);
    }

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

    @Nonnull
    public static String getRealmId(@Nonnull String realmDefId, int index) {
        return realmDefId + DELIMITER_REALM + index;
    }
}
