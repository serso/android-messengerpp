package org.solovyev.android.messenger;

import android.content.Context;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.api.MessengerAsyncTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:22 PM
 */
public abstract class AbstractAsyncLoader<R, LI extends ListItem> extends MessengerAsyncTask<Void, Void, List<R>> {

	@Nonnull
	private final WeakReference<ListAdapter<LI>> adapterRef;

	@Nullable
	private Runnable onPostExecute;

	public AbstractAsyncLoader(@Nonnull Context context,
							   @Nonnull ListAdapter<LI> adapter,
							   @Nullable Runnable onPostExecute) {
		super(context);
		this.adapterRef = new WeakReference<ListAdapter<LI>>(adapter);
		this.onPostExecute = onPostExecute;
	}


	@Override
	protected List<R> doWork(@Nonnull List<Void> voids) {
		final Context context = getContext();
		if (context != null) {
			return getElements(context);
		}

		return Collections.emptyList();
	}

	@Nonnull
	protected abstract List<R> getElements(@Nonnull Context context);

	@Override
	protected void onSuccessPostExecute(@Nullable final List<R> elements) {

		if (elements != null && !elements.isEmpty()) {
			final ListAdapter<LI> adapter = adapterRef.get();
			if (adapter != null) {
				adapter.doWork(new Runnable() {
					@Override
					public void run() {
						for (R element : elements) {
							adapter.add(createListItem(element));
						}
					}
				});
			}
		}

		if (onPostExecute != null) {
			onPostExecute.run();
		}
	}

	@Nullable
	public ListAdapter<LI> getAdapter() {
		return adapterRef.get();
	}

	@Nonnull
	protected abstract LI createListItem(@Nonnull R element);
}
