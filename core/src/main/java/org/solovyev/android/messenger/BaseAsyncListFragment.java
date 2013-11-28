package org.solovyev.android.messenger;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import org.solovyev.android.messenger.accounts.AccountEvent;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.view.MessengerListItem;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.UiThreadEventListener.onUiThread;
import static org.solovyev.common.text.Strings.isEmpty;

public abstract class BaseAsyncListFragment<T, LI extends MessengerListItem> extends BaseListFragment<LI> {

	private static final long SEARCH_DELAY_MILLIS = 500;

	private static final int DEFAULT_MAX_SIZE = 20;

	private static final String BUNDLE_MAX_SIZE = "max_size";

	@Nullable
	private MessengerAsyncTask<Void, Void, List<T>> listLoader;

	private boolean initialLoadingDone = false;

	private int maxSize = DEFAULT_MAX_SIZE;

	private int loadingStartedForTotal = 0;

	private boolean wasFiltered = false;

	@Nonnull
	private final ReloadRunnable runnable = new ReloadRunnable();

	@Nullable
	private JEventListener<UserEvent> userEventListener;

	@Nullable
	private JEventListener<AccountEvent> accountEventListener;

	public BaseAsyncListFragment(@Nonnull String tag, int titleResId, boolean filterEnabled, boolean selectFirstItemByDefault) {
		super(tag, titleResId, filterEnabled, selectFirstItemByDefault);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			maxSize = savedInstanceState.getInt(BUNDLE_MAX_SIZE, DEFAULT_MAX_SIZE);
		}
		startLoading();
	}

	private void startLoading() {
		listLoader = createAsyncLoader(getAdapter(), new OnListLoadedRunnable());
		listLoader.executeInParallel();
	}

	public boolean isInitialLoadingDone() {
		return initialLoadingDone;
	}

	protected int getMaxSize() {
		return maxSize;
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

		attachListeners();
	}

	protected void attachListeners() {
		detachListeners();

		userEventListener = onUiThread(this, new UserEventListener());
		getUserService().addListener(userEventListener);

		accountEventListener = onUiThread(this, new AccountEventListener());
		getAccountService().addListener(accountEventListener);
	}

	private void stopLoading() {
		initialLoadingDone = false;

		if (listLoader != null) {
			listLoader.cancel(false);
			listLoader = null;
		}

		detachListeners();
	}

	protected void detachListeners() {
		if (userEventListener != null) {
			getUserService().removeListener(userEventListener);
		}

		if (accountEventListener != null) {
			getAccountService().removeListener(accountEventListener);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (isViewWasCreated()) {
			outState.putInt(BUNDLE_MAX_SIZE, maxSize);
		}
	}

	@Override
	public void onDestroy() {
		stopLoading();
		super.onDestroy();
	}

	@Override
	public void filter(@Nullable CharSequence filterText) {
		if (isInitialLoadingDone()) {
			if (isEmpty(filterText)) {
				if (wasFiltered) {
					// in case of empty query we need to reset maxSize
					maxSize = DEFAULT_MAX_SIZE;
					loadingStartedForTotal = 0;
					wasFiltered = false;
					postFilter();
				}
			} else {
				wasFiltered = true;
				postFilter();
			}
		}
	}

	private void postFilter() {
		final Handler handler = getUiHandler();
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, SEARCH_DELAY_MILLIS);
	}

	@Override
	public void onItemReachedFromTop(int position, int total) {
		super.onItemReachedFromTop(position, total);

		final float rate = (float) position / (float) total;
		if (rate > 0.75f) {
			if (loadingStartedForTotal != total) {
				loadingStartedForTotal = total;
				maxSize = 2 * maxSize;
				postReload();
			}
		}
	}

	protected void postReload() {
		getUiHandler().post(runnable);
	}

	private class ReloadRunnable implements Runnable {
		@Override
		public void run() {
			unregisterAdapterChangedObserver();

			final BaseListItemAdapter<LI> adapter = getAdapter();
			final AdapterSelection<LI> selection = adapter.getSelectionHelper().getSelection();
			adapter.unselect();
			createAsyncLoader(adapter, new Runnable() {
				@Override
				public void run() {
					final FragmentActivity activity = getActivity();
					if (activity != null) {
						restoreAdapterSelection(activity, selection);
					}
				}
			}).executeInParallel();
		}
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
