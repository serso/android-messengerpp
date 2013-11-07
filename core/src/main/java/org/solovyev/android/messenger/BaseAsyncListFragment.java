package org.solovyev.android.messenger;

import android.os.Bundle;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.view.MessengerListItem;

public abstract class BaseAsyncListFragment<T, LI extends MessengerListItem> extends BaseListFragment<LI> {

	@Nonnull
	private PostListLoadingRunnable onPostLoading;

	@Nullable
	private MessengerAsyncTask<Void, Void, List<T>> listLoader;

	public BaseAsyncListFragment(@Nonnull String tag, boolean filterEnabled, boolean selectFirstItemByDefault) {
		super(tag, filterEnabled, selectFirstItemByDefault);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final ListItemAdapterSelection<LI> selection = createAndAttachAdapter(savedInstanceState);
		onPostLoading = new PostListLoadingRunnable(selection);
	}

	@Override
	public void onStart() {
		super.onStart();

		this.listLoader = createAsyncLoader(getAdapter(), onPostLoading);
		if (this.listLoader != null) {
			this.listLoader.executeInParallel();
		} else {
			// we need to schedule onPostLoading in order to be after all pending transaction in fragment manager
			getUiHandler().post(onPostLoading);
		}
	}

	@Nullable
	protected abstract MessengerAsyncTask<Void, Void, List<T>> createAsyncLoader(@Nonnull BaseListItemAdapter<LI> adapter, @Nonnull Runnable onPostExecute);

	@Nullable
	protected MessengerAsyncTask<Void, Void, List<T>> createAsyncLoader(@Nonnull BaseListItemAdapter<LI> adapter) {
		return createAsyncLoader(adapter, new EmptyRunnable());
	}

	@Override
	public void onStop() {
		if (listLoader != null) {
			listLoader.cancel(false);
			listLoader = null;
		}

		super.onStop();
	}

	private class PostListLoadingRunnable implements Runnable {

		@Nonnull
		private final ListItemAdapterSelection<LI> selection;

		public PostListLoadingRunnable(@Nonnull ListItemAdapterSelection<LI> selection) {
			this.selection = selection;
		}

		@Override
		public void run() {
			// change adapter state
			getAdapter().setInitialized(true);
			runPostFilling();
		}

		private void runPostFilling() {
			// apply filter if any
			onListLoaded(selection);
		}
	}

	private static class EmptyRunnable implements Runnable {
		@Override
		public void run() {
		}
	}
}
