package org.solovyev.android.messenger;

import android.os.Bundle;
import android.view.View;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.view.MessengerListItem;
import org.solovyev.common.listeners.JEventListener;

public abstract class BaseStaticListFragment<LI extends MessengerListItem> extends BaseListFragment<LI> {

	@Nullable
	private JEventListener<UserEvent> userEventListener;

	public BaseStaticListFragment(@Nonnull String tag, boolean filterEnabled, boolean selectFirstItemByDefault) {
		super(tag, filterEnabled, selectFirstItemByDefault);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		userEventListener = new UserEventListener();
		getUserService().addListener(userEventListener);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);
		onListLoaded();
	}

	@Override
	public void onDestroy() {
		if (userEventListener != null) {
			getUserService().removeListener(userEventListener);
		}

		super.onDestroy();
	}
}
