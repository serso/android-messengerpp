package org.solovyev.android.messenger;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.view.MessengerListItem;

public abstract class BaseAsyncListFragment<T, LI extends MessengerListItem> extends BaseListFragment<LI> {

	@Nullable
	private MessengerAsyncTask<Void, Void, List<T>> listLoader;

	private boolean initialLoadingDone = false;

	public BaseAsyncListFragment(@Nonnull String tag, boolean filterEnabled, boolean selectFirstItemByDefault) {
		super(tag, filterEnabled, selectFirstItemByDefault);
	}

	@Override
	public void onStart() {
		super.onStart();

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
	public void onStop() {
		initialLoadingDone = false;

		if (listLoader != null) {
			listLoader.cancel(false);
			listLoader = null;
		}

		super.onStop();
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
