package org.solovyev.android.messenger;

import android.os.Bundle;

import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.view.MessengerListItem;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class BaseAsyncListFragment<T, LI extends MessengerListItem> extends BaseListFragment<LI> {

	private final boolean loadOnCreate;

	@Nullable
	private MessengerAsyncTask<Void, Void, List<T>> listLoader;

	private boolean initialLoadingDone = false;

	@Nullable
	private JEventListener<UserEvent> userEventListener;

	public BaseAsyncListFragment(@Nonnull String tag, int titleResId, boolean filterEnabled, boolean selectFirstItemByDefault) {
		this(tag, titleResId, filterEnabled, selectFirstItemByDefault, false);
	}

	public BaseAsyncListFragment(@Nonnull String tag, int titleResId, boolean filterEnabled, boolean selectFirstItemByDefault, boolean loadOnCreate) {
		super(tag, titleResId, filterEnabled, selectFirstItemByDefault);
		this.loadOnCreate = loadOnCreate;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (loadOnCreate) {
			startLoading();
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if (!loadOnCreate) {
			startLoading();
		}
	}

	private void startLoading() {
		listLoader = createAsyncLoader(getAdapter(), new OnListLoadedRunnable());
		listLoader.executeInParallel();
	}

	public boolean isInitialLoadingDone() {
		return initialLoadingDone;
	}

	@Nonnull
	protected abstract MessengerAsyncTask<Void, Void, List<T>> createAsyncLoader(@Nonnull BaseListItemAdapter<LI> adapter, @Nonnull Runnable onPostExecute);

	@Nullable
	protected MessengerAsyncTask<Void, Void, List<T>> createAsyncLoader(@Nonnull BaseListItemAdapter<LI> adapter) {
		return createAsyncLoader(adapter, new EmptyRunnable());
	}

	@Override
	protected void onListLoaded() {
		super.onListLoaded();

		userEventListener = new UserEventListener();
		getUserService().addListener(userEventListener);
	}

	@Override
	public void onStop() {
		if (!loadOnCreate) {
			stopLoading();
		}

		super.onStop();
	}

	private void stopLoading() {
		initialLoadingDone = false;

		if (listLoader != null) {
			listLoader.cancel(false);
			listLoader = null;
		}

		if (userEventListener != null) {
			getUserService().removeListener(userEventListener);
		}
	}

	@Override
	public void onDestroy() {
		if (loadOnCreate) {
			stopLoading();
		}
		super.onDestroy();
	}

	private class OnListLoadedRunnable implements Runnable {

		@Override
		public void run() {
			initialLoadingDone = true;
			onListLoaded();
		}
	}

	private static class EmptyRunnable implements Runnable {
		@Override
		public void run() {
		}
	}
}
