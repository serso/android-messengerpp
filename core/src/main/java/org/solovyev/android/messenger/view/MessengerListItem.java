package org.solovyev.android.messenger.view;

import javax.annotation.Nonnull;

import org.solovyev.android.list.ListItem;

/**
 * User: serso
 * Date: 3/24/13
 * Time: 11:55 AM
 */
public interface MessengerListItem extends ListItem {

	@Nonnull
	String getId();

	@Nonnull
	CharSequence getDisplayName();

}
