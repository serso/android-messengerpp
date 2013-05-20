package org.solovyev.android.messenger.chats;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.AbstractMessengerEntity;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 7:59 PM
 */
public class ChatImpl extends AbstractMessengerEntity implements Chat {

    /*
	**********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	private boolean privateChat;

	@Nonnull
	private List<AProperty> properties;

	@Nonnull
	private Map<String, String> propertiesMap = new HashMap<String, String>();

	@Nullable
	private DateTime lastMessageSyncDate;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	private ChatImpl(@Nonnull Entity entity,
					 @Nonnull List<AProperty> properties,
					 @Nullable DateTime lastMessageSyncDate) {
		super(entity);
		this.lastMessageSyncDate = lastMessageSyncDate;

		this.properties = properties;

		this.privateChat = true;
		for (AProperty property : properties) {
			this.propertiesMap.put(property.getName(), property.getValue());
			if (property.getName().equals(PROPERTY_PRIVATE)) {
				this.privateChat = Boolean.valueOf(property.getValue());
			}
		}
	}

	private ChatImpl(@Nonnull Entity entity,
					 boolean privateChat) {
		super(entity);
		this.privateChat = privateChat;
		this.properties = new ArrayList<AProperty>();
		final AProperty property = Properties.newProperty(PROPERTY_PRIVATE, Boolean.toString(privateChat));
		properties.add(property);
		propertiesMap.put(property.getName(), property.getValue());
	}


	@Nonnull
	public static Chat newFakeChat(@Nonnull String chatId) {
		return new ChatImpl(EntityImpl.fromEntityId(chatId), false);
	}

	@Nonnull
	public static Chat newInstance(@Nonnull Entity entity,
								   @Nonnull List<AProperty> properties,
								   @Nullable DateTime lastMessageSyncDate) {
		return new ChatImpl(entity, properties, lastMessageSyncDate);
	}

	@Nonnull
	public static Chat newPrivate(@Nonnull Entity entity) {
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
	public List<AProperty> getProperties() {
		return Collections.unmodifiableList(properties);
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
	public Chat copyWithNew(@Nonnull Entity realmChat) {
		return new ChatImpl(realmChat, this.properties, this.lastMessageSyncDate);
	}

	@Nullable
	@Override
	public String getPropertyValueByName(@Nonnull String name) {
		return propertiesMap.get(name);
	}

	@Nonnull
	@Override
	public ChatImpl clone() {
		final ChatImpl clone = (ChatImpl) super.clone();

        /*clone.messages = new ArrayList<ChatMessage>(this.messages.size());
        for (ChatMessage message : this.messages) {
            clone.messages.add(message.clone());
        }

        clone.participants = new ArrayList<User>(this.participants.size());
        for (User participant : this.participants) {
            clone.participants.add(participant.clone());
        }*/

		// properties cannot be changed themselves but some can be removed or added
		clone.properties = new ArrayList<AProperty>(this.properties);
		clone.propertiesMap = new HashMap<String, String>(this.propertiesMap);

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

		return MessengerApplication.getServiceLocator().getChatService().getSecondUser(this);
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
}
