package org.solovyev.android.messenger.preferences;

import android.content.Context;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.MessengerListItemAdapter;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 6:04 PM
 */
public class PreferencesAdapter extends MessengerListItemAdapter<PreferenceGroupListItem> {

	public PreferencesAdapter(@Nonnull Context context, @Nonnull List<? extends PreferenceGroupListItem> listItems) {
		super(context, listItems);
	}
}

