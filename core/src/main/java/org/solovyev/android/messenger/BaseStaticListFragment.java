package org.solovyev.android.messenger;

import android.os.Bundle;
import android.view.View;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.view.MessengerListItem;

public abstract class BaseStaticListFragment<LI extends MessengerListItem> extends BaseListFragment<LI> {

	@Nonnull
	private ListItemAdapterSelection<LI> selection;

	public BaseStaticListFragment(@Nonnull String tag, boolean filterEnabled, boolean selectFirstItemByDefault) {
		super(tag, filterEnabled, selectFirstItemByDefault);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selection = createAndAttachAdapter(savedInstanceState);
		getAdapter().setInitialized(true);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);
		onListLoaded(selection);
	}
}
