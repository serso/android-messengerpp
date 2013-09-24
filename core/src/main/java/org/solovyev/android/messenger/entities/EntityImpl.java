package org.solovyev.android.messenger.entities;

import android.os.Parcel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.JCloneable;
import org.solovyev.common.JObject;
import org.solovyev.common.text.Strings;

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

	public static final Creator<Entity> CREATOR = new Creator<Entity>() {
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
	private String accountId;

	@Nullable
	private String realmId;

	@Nonnull
	private String accountEntityId;

	@Nullable
	private String appAccountEntityId;

	@Nonnull
	private String entityId;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	private EntityImpl(@Nonnull String accountId, @Nonnull String accountEntityId, @Nonnull String entityId) {
		this.accountId = accountId;
		this.accountEntityId = accountEntityId;
		this.entityId = entityId;
	}

	private EntityImpl(@Nonnull String accountId,
					   @Nullable String realmId,
					   @Nonnull String accountEntityId,
					   @Nonnull String entityId) {
		this.accountId = accountId;
		this.realmId = realmId;
		this.accountEntityId = accountEntityId;
		this.entityId = entityId;
	}

	@Nonnull
	public static EntityImpl newEntity(@Nonnull String accountId, @Nonnull String accountEntityId, @Nonnull String entityId) {
		if (Strings.isEmpty(accountId)) {
			throw new IllegalArgumentException("Account cannot be empty!");
		}

		if (Strings.isEmpty(accountEntityId)) {
			throw new IllegalArgumentException("Account entity id cannot be empty!");
		}

		if (Strings.isEmpty(entityId)) {
			throw new IllegalArgumentException("Entity id cannot be empty!");
		}

		return new EntityImpl(accountId, accountEntityId, entityId);
	}

	@Nonnull
	public static Entity newEntity(@Nonnull String accountId, @Nonnull String accountEntityId) {
		return newEntity(accountId, accountEntityId, generateEntityId(accountId, accountEntityId));
	}

	@Nonnull
	public static String generateEntityId(@Nonnull String accountId, String appAccountEntityId) {
		return accountId + DELIMITER + appAccountEntityId;
	}

	@Nonnull
	public static Entity fromEntityId(@Nonnull String entityId) {
		final int index = entityId.indexOf(DELIMITER);
		if (index >= 0) {
			final String realmId = entityId.substring(0, index);
			final String realmUserId = entityId.substring(index + 1);
			return newEntity(realmId, realmUserId);
		} else {
			throw new IllegalArgumentException("No account id is stored in entityId!");
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
	public String getAccountId() {
		return this.accountId;
	}

	@Nonnull
	@Override
	public String getRealmId() {
		if (this.realmId == null) {
			final int index = accountId.indexOf(DELIMITER_REALM);
			if (index >= 0) {
				this.realmId = entityId.substring(0, index);
			} else {
				throw new IllegalArgumentException("No realm id is stored in accountId!");
			}

		}
		return this.realmId;
	}

	@Nonnull
	public String getAccountEntityId() {
		return this.accountEntityId;
	}

	@Nonnull
	@Override
	public String getAppAccountEntityId() {
		if (appAccountEntityId == null) {
			final int index = entityId.indexOf(DELIMITER);
			if (index >= 0) {
				appAccountEntityId = entityId.substring(index + 1);
			} else {
				throw new IllegalArgumentException("No realm is stored in entityId!");
			}
		}

		return appAccountEntityId;
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
		out.writeString(accountId);
		out.writeString(realmId);
		out.writeString(accountEntityId);
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
