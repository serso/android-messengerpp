package org.solovyev.android.messenger.chats;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.AbstractIdentifiable;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;
import org.solovyev.android.properties.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;

import static org.solovyev.android.properties.Properties.newProperties;

public class ChatImpl extends AbstractIdentifiable implements MutableChat {

    /*
	**********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	private boolean privateChat;

	@Nonnull
	private MutableAProperties properties;

	@Nullable
	private DateTime lastMessageSyncDate;

    /*
	**********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	ChatImpl(@Nonnull Entity entity,
			 @Nonnull Collection<AProperty> properties,
			 @Nullable DateTime lastMessageSyncDate) {
		super(entity);
		this.lastMessageSyncDate = lastMessageSyncDate;

		this.properties = newProperties(properties);

		this.privateChat = true;

		final String privateProperty = this.properties.getPropertyValue(PROPERTY_PRIVATE);
		if (privateProperty != null) {
			this.privateChat = Boolean.valueOf(privateProperty);
		}
	}

	ChatImpl(@Nonnull Entity entity,
			 boolean privateChat) {
		super(entity);
		this.privateChat = privateChat;
		this.properties = newProperties(Collections.<AProperty>emptyList());
		this.properties.setProperty(PROPERTY_PRIVATE, Boolean.toString(privateChat));
	}

	@Nonnull
	static MutableChat newPrivateChat(@Nonnull Entity entity) {
		final List<AProperty> properties = new ArrayList<AProperty>();
		properties.add(Properties.newProperty(PROPERTY_PRIVATE, Boolean.toString(true)));
		return new ChatImpl(entity, properties, null);
	}

    /*
    **********************************************************************
    *
    *                           METHODS
    *
    **********************************************************************
    */

	@Nonnull
	public Collection<AProperty> getPropertiesCollection() {
		return properties.getPropertiesCollection();
	}

	@Nonnull
	@Override
	public ChatImpl updateMessagesSyncDate() {
		final ChatImpl clone = clone();

		clone.lastMessageSyncDate = DateTime.now();

		return clone;
	}

	@Nonnull
	@Override
	public Chat copyWithNewId(@Nonnull Entity id) {
		return new ChatImpl(id, this.properties.getPropertiesCollection(), this.lastMessageSyncDate);
	}

	@Nonnull
	@Override
	public Chat cloneWithNewProperty(@Nonnull AProperty property) {
		final ChatImpl clone = clone();
		clone.properties.setProperty(property);
		return clone;
	}

	@Nullable
	@Override
	public String getPropertyValueByName(@Nonnull String name) {
		return properties.getPropertyValue(name);
	}

	@Nonnull
	@Override
	public ChatImpl clone() {
		final ChatImpl clone = (ChatImpl) super.clone();

		clone.properties = this.properties.clone();

		return clone;
	}

	@Override
	public boolean isPrivate() {
		return privateChat;
	}

	@Nonnull
	@Override
	public Entity getSecondUser() {
		assert isPrivate();

		return App.getChatService().getSecondUser(this);
	}

	@Override
	public DateTime getLastMessagesSyncDate() {
		return this.lastMessageSyncDate;
	}

	@Override
	public String toString() {
		return "ChatImpl{" +
				"id=" + getEntity().getEntityId() +
				", privateChat=" + privateChat +
				'}';
	}

	@Nonnull
	@Override
	public Chat merge(@Nonnull Chat that) {
		if(this == that) {
			return this;
		} else {
			final ChatImpl clone = this.clone();
			clone.properties.setPropertiesFrom(that.getPropertiesCollection());
			return clone;
		}
	}
}
