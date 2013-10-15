package org.solovyev.android.messenger.chats;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 7:38 PM
 */
public interface Chat extends Identifiable, EntityAware {

	String PROPERTY_PRIVATE = "private";
	String PROPERTY_DRAFT_MESSAGE = "draft_message";

	@Nonnull
	Entity getEntity();

	boolean isPrivate();

	// must be called only after isPrivate() check
	@Nonnull
	Entity getSecondUser();

	@Nullable
	DateTime getLastMessagesSyncDate();

	@Nonnull
	Collection<AProperty> getPropertiesCollection();

	@Nonnull
	Chat updateMessagesSyncDate();

	/**
	 * Method creates copy of this object with new account id
	 *
	 * @param id new chat id
	 * @return chat copy with updated properties
	 */
	@Nonnull
	Chat copyWithNewId(@Nonnull Entity id);

	@Nonnull
	Chat cloneWithNewProperty(@Nonnull AProperty property);

	@Nullable
	String getPropertyValueByName(@Nonnull String name);
}
