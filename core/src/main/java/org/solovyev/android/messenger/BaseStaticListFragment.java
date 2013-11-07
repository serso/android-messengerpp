package org.solovyev.android.messenger;

import android.os.Bundle;
import android.view.View;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.view.MessengerListItem;

public abstract class BaseStaticListFragment<LI extends MessengerListItem> extends BaseListFragment<LI> {

	public BaseStaticListFragment(@Nonnull String tag, boolean filterEnabled, boolean selectFirstItemByDefault) {
		super(tag, filterEnabled, selectFirstItemByDefault);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);
		onListLoaded();
	}
}
