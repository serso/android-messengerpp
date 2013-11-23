package org.solovyev.android.messenger;

import android.os.Bundle;
import android.view.View;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.view.MessengerListItem;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseStaticListFragment<LI extends MessengerListItem> extends BaseListFragment<LI> {

	@Nullable
	private JEventListener<UserEvent> userEventListener;

	public BaseStaticListFragment(@Nonnull String tag, int titleResId, boolean filterEnabled, boolean selectFirstItemByDefault) {
		super(tag, titleResId, filterEnabled, selectFirstItemByDefault);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		userEventListener = new UserEventListener();
		getUserService().addListener(userEventListener);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		setOnListLoadedCallNeeded(true);
		super.onViewCreated(root, savedInstanceState);
	}

	@Override
	public void onDestroy() {
		if (userEventListener != null) {
			getUserService().removeListener(userEventListener);
		}

		super.onDestroy();
	}
}
