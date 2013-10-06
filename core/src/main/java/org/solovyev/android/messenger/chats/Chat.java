package org.solovyev.android.messenger.chats;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 7:38 PM
 */
public interface Chat extends Identifiable, EntityAware {

	String PROPERTY_PRIVATE = "private";

	@Nonnull
	Entity getEntity();

	boolean isPrivate();

	// must be called only after isPrivate() check
	@Nonnull
	Entity getSecondUser();

	@Nullable
	DateTime getLastMessagesSyncDate();

	@Nonnull
	List<AProperty> getProperties();

	@Nonnull
	Chat updateMessagesSyncDate();

	/**
	 * Method creates copy of this object with new account id
	 *
	 * @param accountChat new chat id
	 * @return chat copy with updated properties
	 */
	@Nonnull
	Chat copyWithNew(@Nonnull Entity accountChat);

	@Nullable
	String getPropertyValueByName(@Nonnull String name);
}
